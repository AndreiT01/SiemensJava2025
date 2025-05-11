package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * Check if item list is empty and return 204 NO_CONTENT if so
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = itemService.findAll();
        if (items.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(items, HttpStatus.OK);    }

    /**
    * Changed the body of the ResponseEntity so it can return a string with the error messages
    * Updated to return BAD_REQUEST on failure and CREATED on success
    */
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("Item creation failed" + result.getAllErrors(),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    /**
     * Added BindingResult for validation error handling
     * If there are any validation errors it returns 400 with validation errors
     * If the item is not found it returns 404 not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return itemService.findById(id)
                .map(existing -> {
                    item.setId(id);
                    return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    /**
     * Updated the delete endpoint to check existence and return appropriate status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.findById(id).isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Return ACCEPTED to reflect async nature of processing
     */
    @GetMapping("/process")
    public ResponseEntity<CompletableFuture<List<Item>>> processItems() {
        return new ResponseEntity<>(itemService.processItemsAsync(), HttpStatus.ACCEPTED);
    }
}
