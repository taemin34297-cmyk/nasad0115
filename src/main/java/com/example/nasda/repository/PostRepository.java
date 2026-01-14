package com.example.nasda.repository;

import com.example.nasda.domain.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {

    long countByUser_UserId(Integer userId);

    List<PostEntity> findTop4ByUser_UserIdOrderByCreatedAtDesc(Integer userId);

    // 단순 버전
    List<PostEntity> findAllByOrderByCreatedAtDesc();

    // ✅ 홈 최적화: 최신 N개만
    List<PostEntity> findTop30ByOrderByCreatedAtDesc();

    // ✅ 추천: N+1 방지용 fetch join (필요 시 사용)
    @Query("""
        select p
        from PostEntity p
        join fetch p.user
        join fetch p.category
        order by p.createdAt desc
    """)
    List<PostEntity> findAllWithUserAndCategoryOrderByCreatedAtDesc();
}
