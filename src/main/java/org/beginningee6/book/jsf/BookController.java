package org.beginningee6.book.jsf;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.beginningee6.book.ejb.BookEJB;
import org.beginningee6.book.jpa.Book;

/**
 * 書籍情報登録画面の画面表示とこの画面からの
 * 登録実行を処理するマネージドBean。
 */
@ManagedBean
@RequestScoped
public class BookController {
	
	// コンテナによって注入されるBookEJB
	@EJB
	BookEJB bookEJB;
	
	// マネージドBeanのプロパティ：登録しようとする書籍情報
	private Book book = new Book();
	// マネージドBeanのプロパティ：永続化されているBookエンティティのリスト
	private List<Book> bookList = new ArrayList<Book>();
	
	/**
	 * 書籍登録画面（newBook.xhtml）でフォームをサブミットした時に
	 * 実行されるアクションメソッド。
	 * 
	 * 書籍登録画面に入力した属性情報でBookエンティティを永続化する。
	 * その後、永続化されているBookエンティティを全件取得し、outcomeとして
	 * listBooks.xhtmlを返すことによって書籍一覧画面を表示する。
	 * 
	 * @return 書籍一覧画面のoutcome（listBooks.xhtml）
	 */
	public String doCreateBook() {

		// Bookエンティティを永続化する
		book = bookEJB.createBook(book);
		
		// 永続化されているBookエンティティを全件取得する
		bookList = bookEJB.findBooks();
		
		// 次に表示する画面を決定する
		return "listBooks.xhtml";
	}

	/**
	 * bookプロパティの値を取得する。
	 * 
	 * @return 保持しているBookエンティティ
	 */
	public Book getBook() {
		
		return book;
	}

	/**
	 * bookプロパティに値を設定する。
	 * 
	 * @param book マネージドBeanに設定するBookエンティティ
	 */
	public void setBook(Book book) {
		this.book = book;
	}

	/**
	 * bookListプロパティの値を取得する。
	 * 
	 * @return 保持しているBookエンティティのリスト
	 */
	public List<Book> getBookList() {
		return bookList;
	}

	/**
	 * bookListプロパティに値を設定する。
	 * 
	 * @param book マネージドBeanに設定するBookエンティティのリスト
	 */
	public void setBookList(List<Book> bookList) {
		this.bookList = bookList;
	}
}
