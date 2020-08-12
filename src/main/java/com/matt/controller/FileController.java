package com.matt.controller;

import com.matt.entity.User;
import com.matt.entity.UserFile;
import com.matt.service.UserFileService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("file")
public class FileController {

    @Autowired
    private UserFileService userFileService;

    /**
     * 返回当前用户所有文件列表--json格式数据
     */

    @GetMapping("findAllJSON")
    @ResponseBody
    public List<UserFile> findAllJSON(HttpSession session,Model model){
//        在登录的session中获取用户的id
        User user = (User) session.getAttribute("user");
//      根据用户id去查询有的文件信息
        List<UserFile> userFiles = userFileService.findByUserId(user.getId());
        return userFiles;
    }

    /**
     * 删除文件信息
     */
    @GetMapping("delete")
    public String delete(String id) throws FileNotFoundException {
//        根据id查询用户信息
        UserFile userFile = userFileService.findById(id);
        String realPath = ResourceUtils.getURL("classpath:").getPath() + "/static" + userFile.getPath();
        File file = new File(realPath,userFile.getNewFileName());
        if (file.exists())file.delete();//立即删除
//        删除数据库
        userFileService.delete(id);


        return "redirect:/file/showAll";
    }


    /**
     * 文件下载
     * @param id
     */
    @GetMapping("download")
    public void download(String openStyle, String id, HttpServletResponse response) throws IOException {
        //        获取打开方式
        openStyle = openStyle == null ? "attachment" : openStyle;


//        获取文件信息
       UserFile userFile = userFileService.findById(id);
//       根据文件信息中文件名字 和 文件存储路径获取文件输入流
       String realpath = ResourceUtils.getURL("classpath:").getPath() + "/static" + userFile.getPath();
//        获取文件输入流
        FileInputStream inputStream = new FileInputStream(new File(realpath, userFile.getNewFileName()));
//        附件下载
        response.setHeader("content-disposition",openStyle+";fileName="+ URLEncoder.encode(userFile.getOldFileName(),"UTF-8"));
//        获取响应流
        ServletOutputStream outputStream = response.getOutputStream();
//        文件拷贝
        IOUtils.copy(inputStream,outputStream);
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
//        更新下载次数,切判断是否为下载方式
        if ("attachment".equals(openStyle)){
            userFile.setDownCounts(userFile.getDownCounts()+1);
            userFileService.update(userFile);
        }
    }






    @PostMapping("upload")
//    名字必须要与表单中的name属性对应上
    public String upload(MultipartFile aaa,HttpSession session) throws IOException {

        User user = (User) session.getAttribute("user");

//          获取文件的原始名称
        String originalFilename = aaa.getOriginalFilename();
//          获取文件后缀
        String extension = "." + FilenameUtils.getExtension(aaa.getOriginalFilename());
//          生成新文件名称
        String newFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + UUID.randomUUID().toString().replace("-", "") + extension;
//          文件大小
        Long size = aaa.getSize();
//          文件类型
        String type = aaa.getContentType();
//          处理根据日期生成目录
        String realPath = ResourceUtils.getURL("classpath:").getPath() + "/static/files";
        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dateDirPath = realPath + "/" + dateFormat;
        File dateDir = new File(dateDirPath);
        if (!dateDir.exists())dateDir.mkdirs();
//          处理文件上传
        aaa.transferTo(new File(dateDir,newFileName));
//        将文件信息放入数据库中
        UserFile userFile = new UserFile();
        userFile.setOldFileName(originalFilename);
        userFile.setNewFileName(newFileName);
        userFile.setExt(extension);
        userFile.setSize(String.valueOf(size));
        userFile.setType(type);
        userFile.setPath("/files/"+dateFormat);
        userFile.setUserId(user.getId());
        userFileService.save(userFile);
        return "redirect:/file/showAll";
    }


    @GetMapping("showAll")
    public String findAll(HttpSession session, Model model){

//        获取 用户id
        User user = (User)session.getAttribute("user");
//       根据用户id查询所有的文件信息
        List<UserFile> userFiles=  userFileService.findByUserId(user.getId());
//       存入作用域中
        model.addAttribute("files",userFiles);

        return "showAll";
    }
}
