package br.com.soeirosantos.grpc.server;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class HeaderServerInterceptor implements ServerInterceptor {

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
			ServerCall<ReqT, RespT> call, final Metadata requestHeaders, ServerCallHandler<ReqT, RespT> next) {
		Context context = Context.current();
		for (HeadersHolder hh : HeadersHolder.values()) {
			context = context.withValue(hh.getKey(), requestHeaders.get(hh.getMetadata()));
		}
		return Contexts.interceptCall(context, call, requestHeaders, next);
	}


}
