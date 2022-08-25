package org.idea.jrpc.framework.core.common;


import lombok.Data;

//invocation    调用
@Data
public class RpcInvocation {
    private String targetMethod;

    private String targetServiceName;   //请求的目标服务名称 例如com.size.user.UserService

    //请求参数信息
    private Object[] args;

    //用于匹配请求和响应的关键值，当请求从客户端发出的时候，会有一个uuid用于记录发出的请求，待数据返回的时候通过uuid来匹配对应的请求线程
    //并且返回给调用线程
    private String uuid;

    //接口相应的数据塞入到这个字段中（如果是异步调用或者void类型，这里则为空）
    private Object response;
}
