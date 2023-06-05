package ru.elseff.socialmedia.persistense.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.persistense.PostImageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostImageRepository extends JpaRepository<PostImageEntity, Long> {

    Optional<PostImageEntity> findByFilename(String filename);

    List<PostImageEntity> findAllByPost(PostEntity postEntity);
}
