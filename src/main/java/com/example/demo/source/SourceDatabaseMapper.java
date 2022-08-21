package com.example.demo.source;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SourceDatabaseMapper {

    @Select("select column_name from information_schema.columns where table_name = #{tableName} order by ORDINAL_POSITION")
    List<String> findColumnsByTableName(String tableName);

    @Select("select * from ${tableName} limit #{limit} offset ${offset}")
    List<Map<String, Object>> findDataByTableName(
            @Param("tableName") String tableName,
            @Param("limit") Long limit,
            @Param("offset") Long offset);

    //@Select("select table_rows from information_schema.tables where table_name = #{tableName}")
    @Select("select count(*) from ${tableName}")
    Long count(String tableName);
}
