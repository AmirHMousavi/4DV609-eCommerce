package org.ecommerce.item.impl;

import static org.ecommerce.security.ServerSecurity.authenticated;

import static java.util.concurrent.CompletableFuture.completedFuture;

import org.ecommerce.message.api.MessageService;
import org.ecommerce.message.api.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ecommerce.item.api.CreateItemRequest;
import org.ecommerce.item.api.CreateItemResponse;
import org.ecommerce.item.api.Item;
import org.ecommerce.item.api.ItemService;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.NotUsed;
import akka.dispatch.OnSuccess;
import akka.stream.FlowShape;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import play.libs.streams.Accumulator;
import play.mvc.Results;
import scala.concurrent.ExecutionContext;

public class ItemServiceImpl implements ItemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);

	private final PersistentEntityRegistry persistentEntities;
	private final CassandraSession db;
	private ExecutionContext ec;
	Materializer materializer;
	MessageService messageService;

	@Inject
	public ItemServiceImpl(MessageService messageService,
			Materializer materializer, ExecutionContext ec, PersistentEntityRegistry persistentEntities,
			CassandraReadSide readSide, CassandraSession db) {
		this.persistentEntities = persistentEntities;
		this.db = db;
		// this.ec = ec;
		this.materializer = materializer;
		this.messageService = messageService;

		persistentEntities.register(ItemEntity.class);
		readSide.register(ItemEventProcessor.class);
	}

	@Override
	public ServiceCall<NotUsed, Item> getItem(String id) {

		return (req) -> {
			return persistentEntities.refFor(ItemEntity.class, id).ask(GetItem.of()).thenApply(reply -> {
				LOGGER.info(String.format("Looking up item %s", id));
				if (reply.getItem().isPresent()){
					Item item =  reply.getItem().get();
					CompletionStage<String> msg = messageService.getIsSold(id)
							.invoke(NotUsed.getInstance());
					String isSold = msg.toCompletableFuture().join();
					return item.withIsSold(isSold);
				}
				else
					throw new NotFound(String.format("No item found for id %s", id));
			});
		};
	}

	@Override
	public ServiceCall<NotUsed, PSequence<Item>> getAllItems() {
		return (req) -> {
			// LOGGER.info("Looking up all items");
			CompletionStage<PSequence<Item>> result = db
					.selectAll("SELECT * FROM item").thenApply(rows -> {
						List<Item> items = rows.stream()
								.map(row -> Item.of(row.getUUID("itemId"), row.getString("userId"),
										row.getString("name"), row.getString("description"), row.getString("photo"),
										row.getDecimal("price"), row.getString("issold")))
								.collect(Collectors.toList());
						return TreePVector.from(items);
					});
			return result;
		};
	}

	@Override
	public ServiceCall<NotUsed, PSequence<Item>> getAllItemsBy(String userId) {
		return (req) -> {
			// LOGGER.info("Looking up all items");
			CompletionStage<PSequence<Item>> result = db
					.selectAll("SELECT * FROM item where userId = ? ALLOW FILTERING", userId).thenApply(rows -> {
						List<Item> items = rows.stream()
								.map(row -> Item.of(row.getUUID("itemId"), row.getString("userId"),
										row.getString("name"), row.getString("description"), row.getString("photo"),
										row.getDecimal("price"), row.getString("issold")))
								.collect(Collectors.toList());
						return TreePVector.from(items);
					});
			return result;
		};
	}

	@Override
	public ServiceCall<CreateItemRequest, CreateItemResponse> createItem() {
		return authenticated(userId -> request -> {
			LOGGER.info("Creating item: {}", request);
			UUID uuid = UUID.randomUUID();
			return persistentEntities.refFor(ItemEntity.class, uuid.toString()).ask(CreateItem.of(request));
		});
	}

	@Override
	public ServiceCall<NotUsed, ByteString> getImage(String id) {
		final Item item = itemGet(id);
		return req -> {
			try {
				String filename = "images/image_" + id + "_" + item.getPhoto();
				File file = new File(filename);
				FileInputStream is = new FileInputStream(file);
				int noOfBytes = is.available();
				byte[] array = new byte[noOfBytes];
				is.read(array);
				is.close();
				ByteString bytes = ByteString.fromArray(array);
				return completedFuture(bytes);
			} catch (Exception ex) {
			}
			return completedFuture(null);
		};
	}

	private Item itemGet(String id) {
		CompletionStage<Item> stage = persistentEntities.refFor(ItemEntity.class, id).ask(GetItem.of())
				.thenApply(reply -> {
					if (reply.getItem().isPresent())
						return reply.getItem().get();
					else
						throw new NotFound(String.format("No item found for id %s", id));
				});
		Item item = null;
		try {
			item = (Item) stage.toCompletableFuture().get();
		} catch (Exception ex) {
		}
		return item;
	}

	@Override
	public ServiceCall<String, String> setSold(String id) {
		return authenticated(userId -> request -> {
			LOGGER.info("Selling item: {}", request);			
			return persistentEntities.refFor(ItemEntity.class, id)
					.ask(SetItemSold.of(request));
		});
	}
	@Override
	// public ServiceCall<String, String> createImage() {
	public ServiceCall<ByteString, String> createImage(String id) {

		return authenticated(userId -> request -> {

			final Item item = itemGet(id);

			ByteString bytes = (ByteString) request;

			try {

				String foldename = "images/";
				File dir = new File(foldename);
				if (!dir.isDirectory() || !dir.exists())
					dir.mkdir();

				String filename = "images/image_" + id + "_" + item.getPhoto();
				File file = new File(filename);
				if (file.exists())
					file.delete();
				file.createNewFile();

				final FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(bytes.toArray());
				outputStream.flush();
				outputStream.close();

				return completedFuture("Uploaded!");
			} catch (Exception ex) {

			}
			return completedFuture(null);
		});

	}

	@Override
	public ServiceCall<NotUsed, Source<ByteString, ?>> downloadImage(String id) {
		return notused -> {
			final Item item = itemGet(id);
			
			String filename = "images/image_" + id + "_" + item.getPhoto();
			File file = new File(filename);
			
			Source<ByteString, ?> source = FileIO.fromFile(file);
						
//			FileInputStream is = new FileInputStream(file);
//			int noOfBytes = is.available();
//			byte[] array = new byte[noOfBytes];
//			is.read(array);
//			is.close();
//			ByteString bytes = ByteString.fromArray(array);
//			
			return completedFuture(source);
		};
	}

	@Override
	public ServiceCall<Source<ByteString, ?>, String> uploadImage(String id) {
		
		return source -> {
			final Item item = itemGet(id);

			String foldename = "images/";
			File dir = new File(foldename);
			if (!dir.isDirectory() || !dir.exists())
				dir.mkdir();

			String filename = "images/image_" + id + "_" + item.getPhoto();
			File file = new File(filename);
			if (file.exists())
				file.delete();
			try {
				file.createNewFile();

				// final FileOutputStream outputStream = new
				// FileOutputStream(file);

				Sink<ByteString, CompletionStage<IOResult>> sink = FileIO.toFile(file);

				sink = Flow.of(ByteString.class)
						// .map(s -> ByteString.fromString(s.toString() + "\n"))
						.toMat(sink, Keep.right());

				source.runWith(sink, materializer);

				// Sink<ByteString, CompletionStage<akka.Done>> sink = Sink
				// .<ByteString>foreach(bytes ->
				// outputStream.write(bytes.toArray()));

			} catch (Exception ex) {
			}

			// CompletionStage<IOResult> result = source.runWith(sink,
			// materializer);
			// try {
			// IOResult value = result.toCompletableFuture().get();
			// } catch (Exception ex) {
			// }

			// return Accumulator.<ByteString,
			// String>fromSink(sink.mapMaterializedValue(
			// completionStage -> completionStage.thenApplyAsync(results ->
			// "file uploaded")));

			// return source.runWith(sink, materializer).thenApplyAsync(results
			// -> "file uploaded");

			// // // // The sink that writes to the output stream

			// return completedFuture((Accumulator<ByteString, Done>) r);
			// });
			// outputStream.write(request.getBytes());

			return completedFuture("Done, file is uploaded!");
		};
	}
}