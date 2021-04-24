### Spring JPA

#### 메소드명 쿼리

#### NamedQuery

#### @Query

#### 페이징
- 리턴 타입을 `Page`, `Slice` 타입을 지정하면 페이징에 필요한 기능을 제공한다.
- ```java
  // Repository
  // totalCount를 구하는 쿼리를 추가로 실행한다.
  Page<Member> findByAge(int age, Pageable pageable);

  // totalCount를 구하는 쿼리를 직접 지정할 수 있다.
  @Query(value = "select m from Member m left join m.team where m.age = :age", countQuery = "select count(m) from Member m")
  Page<Member> findWithCountByAge(@Param(value = "age") int age, Pageable pageable);

  // totalCount를 구하는 로직을 실행하지 않는다.
  // 다음 페이지 여부를 확인하기 위해 Pageable에서 지정한 size + 1을 size로 수행한다.
  Slice<Member> findSliceByAge(int age, Pageable pageable);    
  
  // Test
  int age = 10;
  PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username")); // Pageable (page, limit, sort)를 지정한다.
  
  Page<Member> page = memberRepository.findWithCountByAge(age, pageRequest);
  ```

#### 벌크 업데이트
- DB에 바로 update를 질의한다. update된 엔티티는 영속화되지 않는 상태이므로
- `@Modifying` 애노테이션을 붙여 update임을 알리고 `clearAutomatically` 속성은 `flush()`, `clear()` update를 바로 수행하도록 하기위해 지정한다. 벌크 연산 후 조회하는 로직이 있다면 꼭 붙여줘야 업데이트된 값이 조회된다.
- ```java
  // Repository
  @Modifying(clearAutomatically = true)
  @Query(value = "update Member m set m.age = m.age + 1 where m.age >= :age")
  int bulkAgePlus(@Param(value = "age") int age);
  ```

#### @EntityGraph
- N+1 문제 = 1:N 관계에서 처음 수행된 쿼리의 결과가 10개가 나왔다면 10개에 관련된 데이터를 가져오기 위해 추가로 10번의 쿼리가 실행는 문제
- `fetch join`을 사용하거나 `@EntityGraph`를 사용하여 N+1 문제를 해결할 수 있다.
- ```
  @EntityGraph(attributePaths = {"team"})   // 내부적으로 fetch join이 된다.
  List<Member> findByUsername(@Param("username") String username);
  ```
- `fetch join` JPQL을 직접 구현하지 않고 `@EntityGraph`로 편리하게 할 수 있다.
##### @NamedEntityGraph
- 엔티티에 `@EntityGraph`를 만들어 이름으로 지정할 수 있다.
- ```java
  // Entity
  @Entity
  @NamedEntityGraph(name = "Member.all", attributeNodes = @NamedAttributeNode("team"))
  public class Member {
    
  }
  
  // Repository
  @EntityGraph("Member.all")
  List<Member> findEntityGraphByUsername(@Param(value = "username") String username);
  ```
  
#### JPA Hint
- JPA 구현체에게 제공하는 힌트를 지정한다. (힌트를 통해 보통 성능 최적화를 할 수 있다.)
- ```java
  // Repository
  @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true")) // readOnly 힌트 제공
  Member findReadOnlyByUsername(String username);
  
  // Test
  Member member = new Member("member1", 10);
  memberRepository.save(member);
  em.flush();
  em.clear();
    
  Member findMember = memberRepository.findById(member.getId()).get();
  findMember.changeUsername("member2");   // 변경 감지를 하기위해 원본이 필요하고 그로인해 추가적이 메모리가 소요된다.
  em.flush();
    
  Member findReadOnlyMember = memberRepository.findReadOnlyByUsername("member1");
  findMember.changeUsername("member2");   // 읽기 전용으로 불러왔으므로 변경 감지가 일어나지 않는다.
    
  em.flush();
  ```

#### 사용자 정의 레포짓토리 구현
- QueryDSL, JDBC등 함께 사용할 때 사용자 정의 레포짓토리를 구현하여 사용한다.
- 사용자 정의 레포짓토리 구현체는 클래스 이름이 인터페이스 이름 + Impl이 되어야 한다.
    - impl 대신 다른 이름으로 변경하고 싶다면 config를 설정하여 변경할 수 있다. (관례를 따르는게 좋으니 변경하지 않는게 좋다)
- 사용자 정의 레포짓토리는 기존 레포짓토리(`MemberRepository`)의 기능을 확장하는 개념
```
// 커스텀 Repository 생성
public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}

// 커스텀 Repository 구현체 생성
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m")
                .getResultList();
    }
}

// 커스텀으로 만든 MemberRepositoryCustom 인터페이스를 extends
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
}
```

#### Auditing
- 등록일, 수정일, 등록자, 수정자에 대한 관리
- 공통적인 기능을 담당하는 Super 클래스를 만들어 상속 받는다.
1. JPA 이용
    - ```java
      @MappedSuperclass
      @Getter
      public class JpaBaseEntity {

        @Column(updatable = false)  // updatable = false로 하게되면 update되지 않는다.
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

        @PrePersist                 // persist전 호출
        public void prePersist() {
          LocalDateTime now = LocalDateTime.now();
          createdDate = now;
          updatedDate = now;
        }

        @PreUpdate
        public void preUpdate() {  // update전 호출
          updatedDate = LocalDateTime.now();
        }
      }
     
    @Entity   
    public class Member extends JpaBaseEntity {  // JpaBaseEntity를 상속받는다.
    
    }
    ```
2. 스프링 JPA 이용    
    - ```java
      @EntityListeners(AuditingEntityListener.class)  // audit 이벤트를 수신하는 리스너
      @MappedSuperclass
      @Getter
      public class BaseEntity {
          @CreatedDate
          @Column(updatable = false)
          private LocalDateTime createdDate;
      
          @LastModifiedDate
          private LocalDateTime lastModifiedDate;
      
          @CreatedBy
          @Column(updatable = false)
          private String createBy;
      
          @LastModifiedBy
          private String lastModifiedBy;
      }
      
      @EnableJpaAuditing  // auditing 활성화
      @Configuration
      public class JpaConfig {
          @Bean
          public AuditorAware<String> auditorProvider() {
              return () -> Optional.of(UUID.randomUUID().toString()); // createBy, lastModifiedBy에 들어가는 값을 설정
          }
      }
      ```

#### Web 확장 - 페이징과 정렬
- 페이징과 정렬을 쉽게하기 위한 기능 제공
- page 시작은 0부터 시작한다. (1부터 시작하려면 직접 pageable를 정의 해야한다.)
- ```java
  @GetMapping("/members")
  public Page<Member> list(@PageableDefault(size = 5) Pageable pageable) {   // request parameter page, sort, size를 바인딩한다.
      Page<Member> page = memberRepository.findAll(pageable);
      return page.map(MemberDto::new);
  }
  ```


  
##### Reference
실전! 스프링 데이터 JPA.김영한.인프런강의