package com.example.demo.target;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TargetDatabaseMapper {

    @Select("truncate table ${tableName}")
    List<String> truncate(String tableName);

}
