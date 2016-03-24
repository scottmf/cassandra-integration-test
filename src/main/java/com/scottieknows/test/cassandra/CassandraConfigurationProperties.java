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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cassandra")
public class CassandraConfigurationProperties {

    private String clusterVersion = "3.0.4";
    private String clusterName = "test-cluster";
    private Integer clusterNodes = 3;
    private String hqlInitFiles = null;

    public String getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(String clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(Integer clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public String getHqlInitFiles() {
        return hqlInitFiles;
    }

    public void setHqlInitFiles(String hqlInitFiles) {
        this.hqlInitFiles = hqlInitFiles;
    }

    @Override
    public String toString() {
        return "CassandraConfigurationProperties [clusterVersion=" + clusterVersion + ", clusterName=" + clusterName
            + ", clusterNodes=" + clusterNodes + ", hqlInitFiles=" + hqlInitFiles + "]";
    }

}
