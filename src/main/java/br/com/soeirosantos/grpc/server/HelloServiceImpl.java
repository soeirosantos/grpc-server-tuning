package br.com.soeirosantos.grpc.server;

import io.grpc.stub.StreamObserver;
import org.baeldung.grpc.HelloRequest;
import org.baeldung.grpc.HelloResponse;
import org.baeldung.grpc.HelloServiceGrpc;

public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

	@Override
	public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		String greeting = "Hello, " + request.getFirstName() + " " + request.getLastName();
		HelloResponse response = HelloResponse.newBuilder().setGreeting(greeting).build();
		System.out.println("Header received: " + HeadersHolder.X_THE_HEADER.getKey().get());
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
