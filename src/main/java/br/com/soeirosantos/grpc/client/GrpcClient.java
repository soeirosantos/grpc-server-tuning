package br.com.soeirosantos.grpc.client;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.baeldung.grpc.HelloRequest;
import org.baeldung.grpc.HelloResponse;
import org.baeldung.grpc.HelloServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GrpcClient {

	private final static List<ListenableFuture<HelloResponse>> futures = new ArrayList<>();

	private static final Metadata.Key<String> CUSTOM_HEADER_KEY =
			Metadata.Key.of("X-TheHeader", Metadata.ASCII_STRING_MARSHALLER);

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
				.usePlaintext()
				.build();
		Metadata metadata = getMetadata();
//		executeBlockingTest(channel, metadata);
		executeAsyncTest(channel, metadata);
		channel.shutdown();
	}

	private static void executeBlockingTest(ManagedChannel channel, Metadata metadata) {
		HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
		stub = MetadataUtils.attachHeaders(stub, metadata);
		for (int i = 0; i < 10; i++) {
			long start = System.currentTimeMillis();
			executeCall(stub);
			System.out.println(System.currentTimeMillis() - start);
		}
	}

	private static void executeAsyncTest(ManagedChannel channel, Metadata metadata) throws InterruptedException, ExecutionException {
		HelloServiceGrpc.HelloServiceFutureStub asyncStub = HelloServiceGrpc.newFutureStub(channel);
		asyncStub = MetadataUtils.attachHeaders(asyncStub, metadata);
		for (int j = 0; j < 100; j++) {
			for (int i = 0; i < 10000; i++) {
				executeAsyncCall(asyncStub);
			}
			Thread.sleep(200);
		}
		evaluateFutures();
	}

	private static void executeAsyncCall(HelloServiceGrpc.HelloServiceFutureStub stub) {
		futures.add(stub.hello(HelloRequest.newBuilder().setFirstName("foo").setLastName("bar").build()));
	}

	private static void executeCall(HelloServiceGrpc.HelloServiceBlockingStub stub) {
		HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder().setFirstName("foo").setLastName("bar").build());
		System.out.println(helloResponse.getGreeting());
	}

	private static Metadata getMetadata() {
		Metadata metadata = new Metadata();
		metadata.put(CUSTOM_HEADER_KEY, "My Header Content");
		return metadata;
	}

	private static void evaluateFutures() throws ExecutionException, InterruptedException {
		// wait few seconds to ensure all async requests finished
		Thread.sleep(2000);
		System.out.println("Done: " + futures.stream().filter(ListenableFuture::isDone).count());
	}

}

