package com.example.demo.service;

import com.example.demo.source.SourceDatabaseMapper;
import com.example.demo.target.TargetDatabaseMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DemoService {

    @Autowired
    private SourceDatabaseMapper sourceDatabaseMapper;

    @Autowired
    private TargetDatabaseMapper targetDatabaseMapper;

    @Autowired
    @Qualifier("targetSqlSession")
    private SqlSessionTemplate sqlSessionTemplate;

    @Value("${demo.tables}")
    List<String> tables;

    @Value("${demo.truncate-target}")
    boolean truncateTarget;


    @Value("${demo.batch-count}")
    Long batchCount;

    @Value("${demo.source-schema}")
    String sourceSchema;

    @Value("${demo.target-schema}")
    String targetSchema;

    @Value("${demo.source-where-clause}")
    String sourceWhereClause;


    public Boolean isEmpty(String str){
        if(str==null) return true;
        return str.isEmpty();
    }

    private Logger logger =  LoggerFactory.getLogger(this.getClass());

    private void displayMessage(String message){
        logger.debug(MessageFormat.format(":::{0}", message));
    }
    public void batch() {

        String whereClause = isEmpty(sourceWhereClause) ? " 1 = 1" : sourceWhereClause;

        tables.stream().forEach(table -> {

            var sourceTable = table; // isEmpty(sourceSchema) ? table : MessageFormat.format("{0}.{1}", sourceSchema, table);
            var targetTable = table; // isEmpty(targetSchema) ? table : MessageFormat.format("{0}.{1}", targetSchema, table);

            displayMessage(MessageFormat.format("retrieving meta... {0}.{1}", sourceSchema, sourceTable));
            var columns = sourceDatabaseMapper.findColumnsBySchemaAndTableName(sourceSchema, sourceTable);

            displayMessage(MessageFormat.format("counting... {0}.{1}", sourceSchema, sourceTable));
            var totalCount = sourceDatabaseMapper.count(sourceSchema, table, whereClause);

            if(truncateTarget) {
                displayMessage(MessageFormat.format("truncating....{0}.{1}", targetSchema, targetTable));
                targetDatabaseMapper.truncate(targetSchema, targetTable);
            }

            var pageSize = Math.ceil(totalCount * 1.0 / batchCount);

            var query = MessageFormat.format("INSERT INTO {0}.{1} ({2}) VALUES ({3})",
                    targetSchema,
                    targetTable,
                    String.join(",", columns),
                    columns.stream().map(column -> "?").collect(Collectors.joining(",")));


            try (
                    var sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession();
                    var connection = sqlSession.getConnection();
                    var ps = connection.prepareStatement(query);
            ) {


                for (var page = 0; page < pageSize; page++) {

                    displayMessage(MessageFormat.format("retrieving data... (batch count = {2}).... {0}/{1}", page + 1, pageSize, batchCount));

                    var offset = page * batchCount;
                    var data = sourceDatabaseMapper.findDataByTableName(sourceTable, whereClause, batchCount, offset);

                    for (var i = 0; i < data.size(); i++) {
                        for (var j = 1; j <= columns.size(); j++) {
                            ps.setObject(j, data.get(i).get(columns.get(j - 1)));
                        }
                        ps.addBatch();
                    }

                    displayMessage("executing batch");

                    ps.executeBatch();
                    ps.clearBatch();
                }
                connection.commit();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        });


    }
}
