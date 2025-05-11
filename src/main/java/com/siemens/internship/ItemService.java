package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    // Replaced shared static executor with a private instance-level executor
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    // Used AtomicInteger for thread-safe count tracking
    private final AtomicInteger processedCount = new AtomicInteger(0);

    /**
     * Removed shared processedItems list because using CompletableFuture allows us to build
     * the list from the future chain, eliminating the need for a shared list
     */

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */


    /**
     * Asynchronously processes all items by:
     * Fetching each item's ID from the database
     * Loading the item, updating its status to "PROCESSED", and saving it back
     * Executing each task concurrently using a thread pool
     * Collecting and returning the list of successfully processed items once all are completed
     *
     * This implementation uses CompletableFuture for async execution,
     * ensures thread safety via AtomicInteger, and handles interruption errors properly.
     *
     * @return a CompletableFuture that completes with a list of all successfully processed items
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> ids = itemRepository.findAllIds();

        List<CompletableFuture<Item>> futures = ids.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100);
                        return itemRepository.findById(id).orElse(null);
                    } catch (InterruptedException e) {
                        throw new CompletionException(e);
                    }
                }, executor).thenApply(item -> {
                    if (item != null) {
                        item.setStatus("PROCESSED");
                        processedCount.incrementAndGet();
                        return itemRepository.save(item);
                    }
                    return null;
                }))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

}

