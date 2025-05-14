package com.backfunctionimpl.admin.service;

import com.backfunctionimpl.account.dto.AccountResponseDto;
import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.entity.Image;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.entity.PostTag;
import com.backfunctionimpl.post.repository.PostRepository;
import com.backfunctionimpl.review.dto.ReviewDto;
import com.backfunctionimpl.review.dto.ReviewSummaryDto;
import com.backfunctionimpl.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final ReviewRepository reviewRepository;

    public List<AccountResponseDto> getAllUsers() {
        return accountRepository.findAll().stream()
                .map(account -> new AccountResponseDto(
                        account.getId(),
                        account.getEmail(),
                        account.getNickname(),
                        account.getRole(),
                        account.getImgUrl(),
                        account.getBio(),
                        account.getGender(),
                        account.getBirthday().toString(),
                        account.getLevel().name(),
                        account.getLevelExp()
                ))
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        accountRepository.delete(account);
    }

    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> new PostDto(
                        post.getId(),
                        post.getAccount().getId(),
                        post.getAccount().getNickname(),
                        post.getAccount().getImgUrl(),
                        post.getTitle(),
                        post.getContent(),
                        post.getImages().stream().map(Image::getImageUrl).toList(),
                        post.getCategory(),
                        post.getViews(),
                        post.getCommentsCount(),
                        post.getLikeCount(),
                        post.getReviewSize(),
                        post.getTotalRating(),
                        post.getAverageRating(),
                        post.getTags().stream().map(PostTag::getTagName).toList()
                ))
                .toList();
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        postRepository.delete(post);
    }
}

//    public List<ReviewSummaryDto> getAllReviews() {
//        return reviewRepository.findAll().stream()
//                .map(review -> new ReviewDto(
//                        review.getId(),
//                        review.getPost().getId(),
//                        review.getAccount.getId(),
//                        account.getImgUrl(),
//                        account.getBio(),
//                        account.getGender(),
//                        account.getBirthday().toString(),
//                        account.getLevel().name(),
//                        account.getLevelExp()
//                ))
//                .toList();
//    }

//    public void deleteReview(Long reviewId) {
//    }
//}
