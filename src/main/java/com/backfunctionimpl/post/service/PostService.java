package com.backfunctionimpl.post.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.entity.Image;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.entity.PostTag;
import com.backfunctionimpl.post.repository.PostRepository;
import com.backfunctionimpl.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final S3Service s3Service;

    // 게시글 생성
    public PostDto createPost(PostDto dto, List<MultipartFile> imgs, Account account) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .account(account)
                .reported(false)
                .build();

        List<String> uploadedUrls = new ArrayList<>();
        if (imgs != null) {
            for (MultipartFile img : imgs) {
                String uploadedUrl = s3Service.uploadFile(img);  // This returns full URL
                uploadedUrls.add(uploadedUrl);
            }
        }

        List<Image> images = uploadedUrls.stream()
                .map(url -> Image.builder()
                        .imageUrl(url)
                        .post(post)
                        .build())
                .toList();
        post.setImages(images);

        List<PostTag> tags = dto.getTags().stream()
                .map(tag -> PostTag.builder()
                        .tagName(tag)
                        .post(post)
                        .build())
                .toList();
        post.setTags(tags);

        Post saved = postRepository.save(post);

        account.addExp(15);
        accountRepository.save(account);

        return new PostDto(
                saved.getId(),
                post.getAccount().getId(),
                post.getAccount().getName(),
                post.getAccount().getNickname(),
                post.getAccount().getImgUrl(),
                saved.getTitle(),
                saved.getContent(),
                saved.getImages().stream().map(Image::getImageUrl).toList(),
                saved.getCategory(),
                saved.isReported(),
                saved.getViews(),
                saved.getCommentsCount(),
                saved.getLikeCount(),
                saved.getReviewSize(),
                saved.getTotalRating(),
                saved.getAverageRating(),
                saved.getTags().stream().map(PostTag::getTagName).toList(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // 모든 게시글 조회
    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> new PostDto(
                        post.getId(),
                        post.getAccount().getId(),
                        post.getAccount().getName(),
                        post.getAccount().getNickname(),
                        post.getAccount().getImgUrl(),
                        post.getTitle(),
                        post.getContent(),
                        post.getImages().stream().map(Image::getImageUrl).toList(),
                        post.getCategory(),
                        post.isReported(),
                        post.getViews(),
                        post.getCommentsCount(),
                        post.getLikeCount(),
                        post.getReviewSize(),
                        post.getTotalRating(),
                        post.getAverageRating(),
                        post.getTags().stream().map(PostTag::getTagName).toList(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                ))
                .toList();
    }

    public List<PostDto> getFilteredPosts(String category, String search) {
        // 카테고리와 검색어를 이용해 필터링
        return postRepository.findAll().stream()
                .filter(post -> {
                    boolean matchesCategory = category == null ||
                            category.trim().isEmpty() ||
                            category.equalsIgnoreCase("ALL") ||
                            post.getCategory().name().equalsIgnoreCase(category); // `name()`을 사용하여 비교
                    boolean matchesSearch = search == null ||
                            search.trim().isEmpty() ||
                            post.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                            post.getTags().stream().anyMatch(tag -> tag.getTagName().toLowerCase().contains(search.toLowerCase()));
                    return matchesCategory && matchesSearch;
                })
                .map(post -> new PostDto(
                        post.getId(),
                        post.getAccount().getId(),
                        post.getAccount().getName(),
                        post.getAccount().getNickname(),
                        post.getAccount().getImgUrl(),
                        post.getTitle(),
                        post.getContent(),
                        post.getImages().stream().map(Image::getImageUrl).toList(),
                        post.getCategory(),
                        post.isReported(),
                        post.getViews(),
                        post.getCommentsCount(),
                        post.getLikeCount(),
                        post.getReviewSize(),
                        post.getTotalRating(),
                        post.getAverageRating(),
                        post.getTags().stream().map(PostTag::getTagName).toList(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                ))
                .toList();
    }

    public List<PostDto> getAllUserPosts(Long accountId) {
        return postRepository.findByAccountId(accountId).stream()
                .map(post -> new PostDto(
                        post.getId(),
                        post.getAccount().getId(),
                        post.getAccount().getName(),
                        post.getAccount().getNickname(),
                        post.getAccount().getImgUrl(),
                        post.getTitle(),
                        post.getContent(),
                        post.getImages().stream().map(Image::getImageUrl).toList(),
                        post.getCategory(),
                        post.isReported(),
                        post.getViews(),
                        post.getCommentsCount(),
                        post.getLikeCount(),
                        post.getReviewSize(),
                        post.getTotalRating(),
                        post.getAverageRating(),
                        post.getTags().stream().map(PostTag::getTagName).toList(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                ))
                .toList();
    }

    // 게시글 수정
    @Transactional
    public ResponseEntity<String> updateByPost(
            Long id,
            List<MultipartFile> imgs,
            PostDto dto,
            List<String> remainImgUrls,
            Account account
    ) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getAccount().getId().equals(account.getId())) {
            return ResponseEntity.status(403).body("작성자만 수정할 수 있습니다.");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        post.getImages().clear();
        List<Image> images = new ArrayList<>();

        if (remainImgUrls != null && !remainImgUrls.isEmpty()) {
            for (String url : remainImgUrls) {
                Image img = Image.builder()
                        .imageUrl(url)
                        .post(post)
                        .build();
                images.add(img);
            }
        }

        if (imgs != null && !imgs.isEmpty()) {
            for (MultipartFile imgFile : imgs) {
                String uploadedUrl = s3Service.uploadFile(imgFile);
                Image img = Image.builder()
                        .imageUrl(uploadedUrl)
                        .post(post)
                        .build();
                images.add(img);
            }
        }
        post.getImages().addAll(images);

        post.getTags().clear();
        if (dto.getTags() != null) {
            for (String tagName : dto.getTags()) {
                PostTag tag = PostTag.builder()
                        .tagName(tagName)
                        .post(post)
                        .build();
                post.getTags().add(tag);
            }
        }

        postRepository.save(post);

        return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다.");
    }


    // 게시글 삭제
    public void deleteByPostId(Long id, Long accountId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getAccount().getId().equals(accountId)) {
            throw new SecurityException("작성자만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    // 게시글 상세 조회
    public PostDto findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return new PostDto(
                post.getId(),
                post.getAccount().getId(),
                post.getAccount().getName(),
                post.getAccount().getNickname(),
                post.getAccount().getImgUrl(),
                post.getTitle(),
                post.getContent(),
                post.getImages().stream().map(Image::getImageUrl).toList(),
                post.getCategory(),
                post.isReported(),
                post.getViews(),
                post.getCommentsCount(),
                post.getLikeCount(),
                post.getReviewSize(),
                post.getTotalRating(),
                post.getAverageRating(),
                post.getTags().stream().map(PostTag::getTagName).toList(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // 좋아요 처리
    public void likePost(Long postId, Account account) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 좋아요 수 증가
        post.setLikeCount(post.getLikeCount() + 1);

        // 게시글 저장
        postRepository.save(post);
    }

    public void addReport(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.isReported()) {
            post.setReported(true);
            // 게시글 저장
            postRepository.save(post);
        }
    }

    public void removeReport(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (post.isReported()) {
            post.setReported(false);
            // 게시글 저장
            postRepository.save(post);
        }
    }

    // 뷰 처리
    public void addView(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 뷰 수 증가
        post.setViews(post.getViews() + 1);

        // 게시글 저장
        postRepository.save(post);
    }

}
