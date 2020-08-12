package com.matt.dao;

import com.matt.entity.UserFile;

import java.util.List;

public interface UserFileDAO {
    //根据用户id获得用户列表信息
    List<UserFile> findByUserId(Integer id);
    //保存用户的文件记录
    void save(UserFile userFile);
    //获取文件信息
    UserFile findById(String id);
    //根据id更新下载次数
    void update(UserFile userFile);
    //根据id删除记录
    void delete(String id);
}
