package org.beginningee6.book.jsf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.beginningee6.book.ejb.BookEJB;
import org.beginningee6.book.jpa.Book;
import org.beginningee6.book.jsf.util.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class BookControllerTest {

	@Test
	public void testInitializeBookController() throws Exception {
		// Setup
		BookController controller = new BookController();
		
		// Verify
		Book book = controller.getBook();
		assertThat(book.getDescription(), is(nullValue()));
		assertThat(book.getId(), is(nullValue()));
		assertThat(book.getIllustrations(), is(nullValue()));
		assertThat(book.getIsbn(), is(nullValue()));
		assertThat(book.getNbOfPage(), is(nullValue()));
		assertThat(book.getPrice(), is(nullValue()));
		assertThat(book.getTitle(), is(nullValue()));
		
		assertThat(controller.getBookList().size(), is(0));

	}

	@Test
	public void testSetAndGetBook() throws Exception {
		// Setup
		BookController controller = new BookController();
		
		Book book = new Book();
		book.setDescription("book description");
		book.setIllustrations(true);
		book.setIsbn("book isbn");
		book.setNbOfPage(111);
		book.setPrice(22.2F);
		book.setTitle("book title");
		
		// Exercise and Verify
		controller.setBook(book);
		Book returned = controller.getBook();
		
		assertThat(returned.getDescription(), is("book description"));
		assertThat(returned.getIllustrations(), is(true));
		assertThat(returned.getIsbn(), is("book isbn"));
		assertThat(returned.getNbOfPage(), is(111));
		assertThat(returned.getPrice(), is(22.2F));
		assertThat(returned.getTitle(), is("book title"));
		assertThat(returned.getId(), is(nullValue()));
	}
	
	@Test
	public void testSetAndGetBookList() throws Exception {
		// Setup
		BookController controller = new BookController();
		
		Book book1 = new Book();
		book1.setTitle("book1 title");
		Book book2 = new Book();
		book2.setTitle("book2 title");

		List<Book> bookList = new ArrayList<Book>();
		bookList.add(book1);
		bookList.add(book2);
		
		// Exercise and Verify
		controller.setBookList(bookList);
		List<Book> returned = controller.getBookList();
		
		assertThat(returned.size(), is(2));
		assertThat(returned.get(0).getTitle(), is("book1 title"));
		assertThat(returned.get(1).getTitle(), is("book2 title"));
		
	}
	
	@Test
	public void testCreateBook() throws Exception {
		// Setup
		Book book = new Book();
		book.setDescription("book description");
		book.setIllustrations(true);
		book.setIsbn("book isbn");
		book.setNbOfPage(111);
		book.setPrice(22.2F);
		book.setTitle("book title");
		
		Book persisted1 = new Book();
		persisted1.setDescription("persisted book1 description");
		
		Book persisted2 = new Book();
		persisted2.setDescription("persisted book2 description");
		
		BookEJB bookEJB = mock(BookEJB.class);
		when(bookEJB.createBook(book)).thenReturn(persisted2);
		
		List<Book> bookList = new ArrayList<Book>();
		bookList.add(persisted1);
		bookList.add(persisted2);
		when(bookEJB.findBooks()).thenReturn(bookList);

		BookController controller = new BookController();
		controller.bookEJB = bookEJB;
		controller.setBook(book);
		
		// Exercise
		String outcome = controller.doCreateBook();
		
		assertThat(outcome, is("listBooks.xhtml"));
		
		assertThat(controller.getBook().getDescription(), is("persisted book2 description"));
		assertThat(controller.getBookList().size(), is(2));
		assertThat(controller.getBookList().get(0).getDescription(), is("persisted book1 description"));
		assertThat(controller.getBookList().get(1).getDescription(), is("persisted book2 description"));
		
		verify(bookEJB).createBook(book);
		verify(bookEJB).findBooks();
	}
}
