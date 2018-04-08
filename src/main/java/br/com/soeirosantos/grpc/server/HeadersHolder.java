package br.com.soeirosantos.grpc.server;

import io.grpc.Context;
import io.grpc.Metadata;

public enum HeadersHolder {

	X_THE_HEADER("X-TheHeader");

	private final Context.Key<String> key;
	private final Metadata.Key<String> metadata;

	HeadersHolder(String headerName) {
		this.key = Context.key(headerName);
		this.metadata = Metadata.Key.of(headerName, Metadata.ASCII_STRING_MARSHALLER);
	}

	public Context.Key<String> getKey() {
		return key;
	}

	public Metadata.Key<String> getMetadata() {
		return metadata;
	}
}
