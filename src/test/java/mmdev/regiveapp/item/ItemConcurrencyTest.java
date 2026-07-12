package mmdev.regiveapp.item;

import mmdev.regiveapp.AbstractIntegrationTest;
import mmdev.regiveapp.category.Category;
import mmdev.regiveapp.category.CategoryRepository;
import mmdev.regiveapp.user.Role;
import mmdev.regiveapp.user.User;
import mmdev.regiveapp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ItemConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ItemService itemService;

    private Long itemId;

    @BeforeEach
    void setUp(){
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-"+System.nanoTime() + "@example.com");
        owner.setPassword("hashed");
        owner.setRole(Role.USER);
        owner = userRepository.save(owner);

        Category category = new Category();
        category.setName("Cat- " + System.nanoTime());
        category = categoryRepository.save(category);

        Item item = new Item();
        item.setTitle("Contested item");
        item.setCity("Lviv");
        item.setStatus(ItemStatus.ACTIVE);
        item.setOwner(owner);
        item.setCategory(category);
        itemId = itemRepository.save(item).getId();
    }

    @Test
    @DisplayName("claim: only one of many concurrent requests succeeds")
    void claim_shouldAllowOnlyOneWinner() throws InterruptedException{
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        for (int i = 0; i<threads;i++){
            executor.submit(()->{
               try {
                   startSignal.await();
                   itemService.claim(itemId);
                   successes.incrementAndGet();
               }catch (Exception e){
                   failures.incrementAndGet();
               }finally {
                   done.countDown();
               }
            });
        }
        startSignal.countDown();
        done.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successes.get()).isEqualTo(1);
        assertThat(failures.get()).isEqualTo(threads-1);

        Item reloaded = itemRepository.findById(itemId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ItemStatus.RESERVED);
    }

}
