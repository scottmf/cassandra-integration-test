/**
 * Copyright (C) 2016 Scott Feldstein
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.scottieknows.test.cassandra;

import static java.lang.String.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraClusterManager implements ApplicationListener<ContextRefreshedEvent> {

    private final Log log = LogFactory.getLog(CassandraClusterManager.class);
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String CCM = USER_DIR + File.separator + "ccm-2.1.3";

/*
    @Value("${cassandra-integration-test.clusterVersion:3.0.3}")
    private String clusterVersion;
    @Value("${cassandra-integration-test.clusterName:test-cluster}")
    private String clusterName;
    @Value("${cassandra-integration-test.clusterNodes:1}")
    private Integer clusterNodes;
    @Value("${cassandra-integration-test.keyspace:mykeyspace}")
    private String keyspace;
    @Value("${cassandra-integration-test.keyspaceReplication:{'class':'SimpleStrategy','replication_factor':2}}")
    private String keyspaceReplication;
*/
    @Autowired
    private CassandraConfigurationProperties cassandraConfigurationProperties;
    @Autowired
    private Cluster cluster;

/* it would be nice to dynamically pull down ccm, but there are initialization steps that do not adhere to this
    private static final String CCM_GITHUB_URL = "https://api.github.com/repos/pcmanus/ccm/tarball";
    private static final String JAVA_TMP = System.getProperty("java.io.tmpdir");
    private static final String CCM_DIR = JAVA_TMP + File.separator + "ccm";
    private static final String CCM_OLD_DIR = JAVA_TMP + File.separator + "ccm-old";
    @SuppressWarnings("unused")
    private String pullCcm() throws IOException {
        RestTemplate template = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT_ENCODING, "gzip");
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<Resource> resp = template.exchange(CCM_GITHUB_URL, HttpMethod.GET, httpEntity, Resource.class);
        InputStream is = resp.getBody().getInputStream();
        try (TarArchiveInputStream tais = new TarArchiveInputStream(new GZIPInputStream(is))) {
            File dir = new File(CCM_DIR);
            if (dir.exists()) {
                dir.renameTo(new File(CCM_OLD_DIR));
            }
            dir = new File(CCM_DIR);
            dir.mkdirs();
            TarArchiveEntry entry;
            while (null != (entry = tais.getNextTarEntry())) {
                System.out.println(entry.getName());
                File file = new File(CCM_DIR + entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                    continue;
                }
                byte[] b = new byte[8192];
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    tais.read(b);
                    int read;
                    while (-1 != (read = tais.read(b))) {
                        fos.write(b, 0, read);
                    }
                }
            }
        }
        File ccm = new File(CCM_DIR);
        for (File f : ccm.listFiles()) {
            if (f.isDirectory()) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }
*/

    public void createCluster(String version, int numNodes, String clusterName) {
        exec(format("create --version %s --nodes %s --start %s", version, numNodes, clusterName));
    }

    /**
     * @throws {@link PyException} on python error
     */
    public String exec(String args) {
        final boolean debug = log.isDebugEnabled();
        if (debug) {log.debug("ccm " + args);}
        String[] toks = args.split("\\s+");
        PySystemState state = new PySystemState();
        for (String arg : toks) {
            state.argv.add(arg);
        }
        try (PythonInterpreter interp = new PythonInterpreter(null, state)) {
            StringWriter writer = new StringWriter();
            interp.setOut(writer);
            interp.execfile(CCM + File.separator + "ccm");
            String out = writer.toString();
            if (debug) {log.debug(out);}
            return out;
        }
    }

    /**
     * @return {@link Map} of interface name to address
     */
    Map<String, String> getIpsStartingWith(String prefix) {
        if (prefix == null) {
            return Collections.emptyMap();
        }
        try {
            Map<String, String> rtn = new HashMap<>();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface i = networkInterfaces.nextElement();
                Enumeration<InetAddress> addrs = i.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    String hostAddress = addr.getHostAddress();
                    if (hostAddress.startsWith(prefix)) {
                        rtn.put(i.getName(), hostAddress);
                    }
                }
            }
            return rtn;
        } catch (SocketException e) {
            throw new RuntimeException("could not retrieve network interfaces: " + e, e);
        }
    }

/* XXX
    public void createOrVerifyKeyspace() {
        Session session = cluster.connect();
        if (!instanceContainsKeyspace(session)) {
            String cql = format("create keyspace %s with replication = %s", keyspace, keyspaceReplication);
            session.execute(cql);
        }
    }

    private boolean instanceContainsKeyspace(Session session) {
        ResultSet rs = session.execute("select * from system_schema.keyspaces");
        List<Row> keyspaces = rs.all();
        for (Row row : keyspaces) {
            String keyspaceName = row.getString("keyspace_name");
            if (Objects.equals(keyspaceName, keyspace)) {
                return true;
            }
        }
        return false;
    }
*/

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        CassandraClusterManager ccm = ctx.getBean(CassandraClusterManager.class);
        String out = ccm.exec("list");
        if (!out.contains(cassandraConfigurationProperties.getClusterName() + "\n")) {
            createCluster(cassandraConfigurationProperties.getClusterVersion(), 
                cassandraConfigurationProperties.getClusterNodes(),
                cassandraConfigurationProperties.getClusterName());
        } else if (!out.contains("*" + cassandraConfigurationProperties.getClusterName() + "\n")) {
            exec("switch " + cassandraConfigurationProperties.getClusterName());
        }
        String status = ccm.exec("status");
        if (!status.contains("UP")) {
            exec("start " + cassandraConfigurationProperties.getClusterName());
        }
        execHqlInitFiles(ctx);
// XXX
//        ccm.createOrVerifyKeyspace();
    }

    private void execHqlInitFiles(ApplicationContext ctx) {
        String files = cassandraConfigurationProperties.getHqlInitFiles();
        if (files == null || files.trim().isEmpty()) {
            return;
        }
        Cluster cluster = ctx.getBean(Cluster.class);
        Session session = cluster.connect();
        String[] toks = files.split(",");
        for (String f : toks) {
            try {
                Resource resource = ctx.getResource(f);
                File file = null;
                if (resource == null || (file = resource.getFile()) == null) {
                    continue;
                }
                Collection<String> hqls = getHqls(file);
                hqls.forEach(hql -> session.execute(hql));
            } catch (IOException e) {
                throw new RuntimeException(format("problem initializing hql from file %s: %s", f, e), e);
            }
        }
    }

    private Collection<String> getHqls(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.startsWith("--")) {
                    builder.append(line);
                }
            }
        }
        return Arrays.asList(builder.toString().split(";"));
    }

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        String userDir = System.getProperty("user.dir");
        String pythonPath = format("%s/python-libs/:%s", userDir, CCM);
        props.put("python.path", pythonPath);
        // Used to prevent: console: Failed to install '': java.nio.charset.UnsupportedCharsetException: cp0.
        props.put("python.console.encoding", "UTF-8");
        //don't respect java accessibility, so that we can access protected members on subclasses
        props.put("python.security.respectJavaAccessibility", "false");
        props.put("python.import.site","false");
        Properties preprops = System.getProperties();
        // running with new String[0] causes the args not to work properly
        PythonInterpreter.initialize(preprops, props, new String[] {""});
    }

}
