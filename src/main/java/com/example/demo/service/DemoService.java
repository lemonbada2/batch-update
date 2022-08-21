package com.example.demo.service;

import com.example.demo.source.SourceDatabaseMapper;
import com.example.demo.target.TargetDatabaseMapper;
import org.mybatis.spring.SqlSessionTemplate;
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

    public void batch() {

        StopWatch stopWatch = new StopWatch();

        tables.stream().forEach(table -> {

            var targetTable = table + "_cp";

            System.out.println(MessageFormat.format(">>> retrieving meta....{0}", table));
            var columns = sourceDatabaseMapper.findColumnsByTableName(table);

            System.out.println(MessageFormat.format(">>> counting....{0}", table));
            var totalCount = sourceDatabaseMapper.count(table);

            if(truncateTarget) {
                System.out.println(MessageFormat.format(">>> truncate....{0}", targetTable));
                targetDatabaseMapper.truncate(targetTable);
            }

            var pageSize = Math.ceil(totalCount * 1.0 / batchCount);

            var query = MessageFormat.format("INSERT INTO {0} ({1}) VALUES ({2})",
                    targetTable,
                    String.join(",", columns),
                    columns.stream().map(column -> "?").collect(Collectors.joining(",")));


            try (
                    var sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession();
                    var connection = sqlSession.getConnection();
                    var ps = connection.prepareStatement(query);
            ) {


                for (var page = 0; page < pageSize; page++) {

                    System.out.println(MessageFormat.format(">> retrieving data (batch count = {2}).... {0}/{1}", page + 1, pageSize, batchCount));

                    var offset = page * batchCount;
                    stopWatch.start("[RETRIVE]");
                    var data = sourceDatabaseMapper.findDataByTableName(table, batchCount, offset);
                    stopWatch.stop();
                    System.out.println(">> retrieve : " + stopWatch.getTotalTimeSeconds());

                    stopWatch.start("[GATHER]");
                    for (var i = 0; i < data.size(); i++) {
                        for (var j = 1; j <= columns.size(); j++) {
                            ps.setObject(j, data.get(i).get(columns.get(j - 1)));
                        }
                        ps.addBatch();
                    }
                    stopWatch.stop();
                    System.out.println(">> gather : " + stopWatch.getTotalTimeSeconds());

                    System.out.println(">> executing batch");
                    stopWatch.start("[BATCH]");

                    ps.executeBatch();
                    ps.clearBatch();

                    stopWatch.stop();
                    System.out.println(">> batch : " + stopWatch.getTotalTimeSeconds());
                }

                connection.commit();

            } catch (SQLException e) {
                e.printStackTrace();
            }


        });


    }
}
