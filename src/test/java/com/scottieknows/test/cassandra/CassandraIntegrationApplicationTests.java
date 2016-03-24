package com.scottieknows.test.cassandra;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.scottieknows.test.cassandra.CassandraIntegrationApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CassandraIntegrationApplication.class)
public class CassandraIntegrationApplicationTests {

    @Autowired
    private CassandraClusterManager ccm;
    @Autowired
    private Cluster cluster;

    private final String keyspace = "mykeyspace";
    private final String table = "test";

    @Test
    public void contextLoads() throws InterruptedException {
        Session session = cluster.connect();
        session.execute("drop table if exists mykeyspace.test");
        session.execute("create table mykeyspace.test (a int, b text, primary key(a))");
        Thread.sleep(1000);
        Insert insert = QueryBuilder.insertInto(keyspace, table);
        insert.value("a", 0);
        insert.value("b", "text0");
        session.execute(insert);
        Select select = QueryBuilder.select().all().from(keyspace, table);
        ResultSet rs = session.execute(select);
        Row row = rs.one();
        int a = row.getInt("a");
        String b = row.getString("b");
        assertEquals(0, a);
        assertEquals("text0", b);
    }

}
