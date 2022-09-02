package com.example.demo.source;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SourceDatabaseMapper {

    @Select("select column_name from information_schema.columns where table_schema = #{tableSchema} and table_name = #{tableName} order by ORDINAL_POSITION")
    List<String> findColumnsBySchemaAndTableName(@Param("tableSchema") String tableSchema, @Param("tableName") String tableName);

    @Select("select * from ${tableSchema}.${tableName} where ${where} limit #{limit} offset #{offset}")
    List<Map<String, Object>> findDataByTableName(
            @Param("tableSchema") String tableSchema,
            @Param("tableName") String tableName,
            @Param("where") String where,
            @Param("limit") Long limit,
            @Param("offset") Long offset);

    @Select("select count(*) from ${tableSchema}.${tableName} where ${where}")
    Long count(@Param("tableSchema")String tableSchema,  @Param("tableName") String tableName, @Param("where")String where);
}
