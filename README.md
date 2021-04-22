### Spring JPA

#### 메소드명 쿼리

#### NamedQuery

#### @Query

#### 페이징

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