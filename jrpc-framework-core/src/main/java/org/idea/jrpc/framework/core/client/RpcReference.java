package org.idea.jrpc.framework.core.client;

import org.idea.jrpc.framework.core.proxy.ProxyFactory;
public class RpcReference {
    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {this.proxyFactory = proxyFactory;}

    public <T> T get(Class<T> tClass) throws Throwable {
        return proxyFactory.getProxy(tClass);
    }
}
