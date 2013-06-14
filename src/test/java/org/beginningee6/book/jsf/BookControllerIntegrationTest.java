package org.beginningee6.book.jsf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.beginningee6.book.jpa.Book;
import org.beginningee6.book.jsf.util.IntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jsfunit.api.InitialPage;
import org.jboss.jsfunit.api.JSFUnitResource;
import org.jboss.jsfunit.jsfsession.JSFClientSession;
import org.jboss.jsfunit.jsfsession.JSFServerSession;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@InitialPage("/newBook.faces")
public class BookControllerIntegrationTest {
	@JSFUnitResource
	private JSFClientSession client;
	@JSFUnitResource
	private JSFServerSession server;

	// JSFUnitによるテストを実行するためには、
	// JBossサーバは、standalone.bat -b=192.168.41.75で起動を掛けておくこと。
	// 
	// standalone.xmlの<interfaces>タグでも、以下のようになっていることを確認するこっと。
	// <interfaces>
	// 		<interface name="management">
	//     		<inet-address value="${jboss.bind.address.management:0.0.0.0}"/>　⇒ここ
	// 		</interface>
	// 		<interface name="public">
	//     		<inet-address value="${jboss.bind.address:0.0.0.0}"/>　⇒ここ
	// 		</interface>
	// 		<interface name="unsecure">
	//     		<inet-address value="${jboss.bind.address.unsecure:127.0.0.1}"/>
	// 		</interface>
	// </interfaces>
	@Deployment
	public static WebArchive createDeployment() {
		File dependenciesDir = new File("target/dependency");
		File[] dependencyLibs = dependenciesDir.listFiles();

		WebArchive war = ShrinkWrap
				.create(WebArchive.class, "test.war")
				.setWebXML(new File("src/main/webapp/WEB-INF/web.xml"))
				.addAsWebInfResource(
						new File("src/main/webapp/WEB-INF/faces-config.xml"))
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsWebInfResource("jbossas-ds.xml")
				.addPackage(Package.getPackage("org.beginningee6.book.jsf"))
				.addPackage(
						Package.getPackage("org.beginningee6.book.jsf.util"))
				.addAsLibraries(dependencyLibs)
				.addAsWebResource(new File("src/main/webapp", "newBook.xhtml"))
				.addAsWebResource(
						new File("src/main/webapp", "listBooks.xhtml"));

		return war;
	}
	
	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction userTransaction;

	/**
	 * 各テストの前に対象のデータをクリアしておく。
	 */
	@Before
	public void setUp() throws Exception {
		clearData();
	}

	/**
	 * Bookエンティティに対応するテーブルの全ての行を削除する
	 */
	private void clearData() throws Exception {

		userTransaction.begin(); // トランザクション開始
		em.joinTransaction(); // EntityManagerにトランザクション開始を通知

		// テーブルの全ての行を削除する
		em.createQuery("DELETE FROM Book").executeUpdate();

		userTransaction.commit(); // コミット
	}
	
	@Test
	public void testCreateBook() throws Exception {
		// Setup
		assertThat(server.getCurrentViewID(), is("/newBook.xhtml"));

		client.setValue("isbn", "111-111111-111");
		client.setValue("title", "book1 title");
		client.setValue("price", "111.11");
		client.setValue("description", "book1 description");
		client.setValue("pages", "111");
		client.click("illustrations");

		// Exercise
		client.click("submit");

		// Verify - View ID (page navigation)
		assertThat(server.getCurrentViewID(), is("/listBooks.xhtml"));

		// Verify - Rendering
		UIComponent component = server.findComponent("bookList");
		assertThat(component.isRendered(), is(true));
		
		// Verify - Table row count
		UIData table = (UIData)component;
		assertThat(table.getRowCount(), is(1));
		
		// Verify - Contents of rendered row
		table.setRowIndex(0);
		Book renderedItem = (Book)table.getRowData();

		assertThat(renderedItem.getDescription(), is("book1 description"));
		assertThat(renderedItem.getIllustrations(), is(true));
		assertThat(renderedItem.getIsbn(), is("111-111111-111"));
		assertThat(renderedItem.getNbOfPage(), is(111));
		assertThat(renderedItem.getPrice(), is(111.11F));
		assertThat(renderedItem.getTitle(), is("book1 title"));
		assertThat(renderedItem.getId(), is(notNullValue()));
		
		// Verify - Managed Bean
		Book created = (Book)server.getManagedBeanValue("#{bookController.book}");
		assertThat(created.getDescription(), is("book1 description"));
		assertThat(created.getIllustrations(), is(true));
		assertThat(created.getIsbn(), is("111-111111-111"));
		assertThat(created.getNbOfPage(), is(111));
		assertThat(created.getPrice(), is(111.11F));
		assertThat(created.getTitle(), is("book1 title"));
		assertThat(created.getId(), is(notNullValue()));
		
		@SuppressWarnings("unchecked")
		List<Book> currentBookList = (List<Book>)server.getManagedBeanValue("#{bookController.bookList}");
		assertThat(currentBookList.size(), is(1));
		assertThat(currentBookList.get(0).getId(), is(notNullValue()));
		assertThat(currentBookList.get(0).getTitle(), is("book1 title"));
		
		// Verify - Persistence
		TypedQuery<Book> query = em.createNamedQuery("findAllBooks", Book.class);
		List<Book> persistedBookList = query.getResultList();
		
		assertThat(persistedBookList.size(), is(1));

		Book persisted = persistedBookList.get(0);
		assertThat(persisted.getDescription(), is("book1 description"));
		assertThat(persisted.getIllustrations(), is(true));
		assertThat(persisted.getIsbn(), is("111-111111-111"));
		assertThat(persisted.getNbOfPage(), is(111));
		assertThat(persisted.getPrice(), is(111.11F));
		assertThat(persisted.getTitle(), is("book1 title"));
		assertThat(persisted.getId(), is(notNullValue()));
	}
}
