package com.example.demo.target;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface TargetDatabaseMapper {

    @Select("truncate table ${tableSchema}.${tableName}")
    List<String> truncate(@Param("tableSchema")String tableSchema, @Param("tableName") String tableName);

    @Update("SET GLOBAL foreign_key_checks=OFF")
    void offForeignKeyChecks();

    @Update("SET GLOBAL foreign_key_checks=ON")
    void onForeignKeyChecks();

    @Select("ALTER TABLE ${tableSchema}.${tableName} ENABLE KEYS")
    void enableKeys(@Param("tableSchema")String tableSchema, @Param("tableName") String tableName);

}
