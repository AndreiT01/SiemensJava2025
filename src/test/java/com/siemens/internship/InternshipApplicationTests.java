package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class InternshipApplicationTests {

	// Mock the repository
	@Mock
	private ItemRepository itemRepository;

	// Inject the mock repository into the service
	@InjectMocks
	private ItemService itemService;

	private Item testItem;

	// Initialize mocks and test data before each test
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		testItem = new Item(1L, "Test Name", "Test Desc", "NEW", "test@example.com");
	}

	@Test
	void testFindAll() {
		when(itemRepository.findAll()).thenReturn(List.of(testItem));
		List<Item> result = itemService.findAll();
		assertEquals(1, result.size());
	}

	@Test
	void testFindById() {
		when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
		Optional<Item> result = itemService.findById(1L);
		assertTrue(result.isPresent());
	}

	@Test
	void testSave() {
		when(itemRepository.save(any())).thenReturn(testItem);
		Item saved = itemService.save(testItem);
		assertEquals("Test Name", saved.getName());
	}

	@Test
	void testDeleteById() {
		itemService.deleteById(1L);
		verify(itemRepository, times(1)).deleteById(1L);
	}

	@Test
	void testProcessItemsAsync() throws Exception {
		when(itemRepository.findAllIds()).thenReturn(List.of(1L));
		when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
		when(itemRepository.save(any())).thenReturn(testItem);

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> result = future.get();
		assertEquals(1, result.size());
		assertEquals("PROCESSED", result.get(0).getStatus());
	}

	@Test
	void testFindByIdNotFound() {
		when(itemRepository.findById(99L)).thenReturn(Optional.empty());
		Optional<Item> result = itemService.findById(99L);
		assertTrue(result.isEmpty());
	}

	@Test
	void testSaveWithNullFields() {
		Item incompleteItem = new Item();
		incompleteItem.setEmail("test@domain.com");
		when(itemRepository.save(any())).thenReturn(incompleteItem);

		Item result = itemService.save(incompleteItem);
		assertEquals("test@domain.com", result.getEmail());
	}

	@Test
	void testProcessItemsAsyncWithMissingItem() throws Exception {
		when(itemRepository.findAllIds()).thenReturn(List.of(2L));
		when(itemRepository.findById(2L)).thenReturn(Optional.empty());

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> result = future.get();

		assertTrue(result.isEmpty());
	}

}
