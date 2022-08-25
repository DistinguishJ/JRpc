package org.idea.jrpc.framework.core.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

import static org.idea.jrpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;

@Data
public class RpcProtocol implements Serializable {
    private static final long serialVersionUID = 5359096060555795690L;

    private short magicNumber = MAGIC_NUMBER;

    private int contentLength;

    //核心的传输数据，主要是请求的服务名称，请求服务的方法名称，请求的参数内容，核心请求数据封装到了RpcInvocation对象当中
    private byte[] content;

    public RpcProtocol(byte[] content) {
        this.contentLength = content.length;
        this.content = content;
    }

    public String toString() {
        return "RpcProtocol{" +
                "contentLength=" + contentLength +
                ", content=" + Arrays.toString(content) +
                '}';
    }

}
