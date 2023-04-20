package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.post.Post;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.user.User;
import bg.ittalents.instagram.user.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;


    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository,
                          ModelMapper mapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public Slice<CommentDTO> getCommentReplies(long userId, long commentId, Pageable pageable) {

        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        return commentRepository.findAllRepliesByParentIdOrderByNumberOfLikes(
                userId,
                c.getId(),
                pageable).map(comment -> commentToCommentDTO(comment));
    }

    public CommentDTO addCommentToPost(long userId, long postId, CommentContentDTO commentContentDTO) {

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Post post = postRepository.findByIdAndIsCreatedIsTrue(userId, postId)
                .orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        Comment comment = mapper.map(commentContentDTO, Comment.class);
        comment.setPost(post);
        comment.setDateTimeCreated(LocalDateTime.now());
        comment.setOwner(owner);
        return mapper.map(commentRepository.save(comment), CommentDTO.class);
    }

    @Transactional
    public int likePost(long userId, long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Comment comment = commentRepository.findCommentByIdById(userId, commentId)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        if (user.getLikedComments().contains(comment) || comment.getLikedBy().contains(user)) {
            user.getLikedComments().remove(comment);
            comment.getLikedBy().remove(user);
        } else {
            user.getLikedComments().add(comment);
            comment.getLikedBy().add(user);
        }

        userRepository.save(user);
        commentRepository.save(comment);
        return comment.getLikedBy().size();
    }

    public CommentDTO deleteComment(long userId, long commentId) {

        Comment comment = commentRepository.findCommentByIdById(userId, commentId)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        if (comment.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't delete post that is not yours!");
        }

        CommentDTO dto = commentToCommentDTO(comment);
        commentRepository.delete(comment);
        return dto;
    }

    public CommentDTO commentToCommentDTO(Comment comment) {
        CommentDTO dto = mapper.map(comment, CommentDTO.class);
        dto.setNumberOfLikes(comment.getLikedBy().size());
        dto.setNumberOfReplies(comment.getReplies().size());
        return dto;
    }

    public CommentDTO replyToComment(long userId, long parentId,
                                     CommentContentDTO commentContentDTO) {

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        Comment comment = mapper.map(commentContentDTO, Comment.class);
        comment.setPost(parentComment.getPost());
        comment.setDateTimeCreated(LocalDateTime.now());
        comment.setOwner(owner);
        comment.setParent(parentComment);
        return mapper.map(commentRepository.save(comment), CommentDTO.class);
    }

    public Slice<CommentDTO> getPostComments(long userId, long postId, Pageable pageable) {

        postRepository.findByIdAndIsCreatedIsTrue(userId, postId)
                .orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        return commentRepository.findAllParentCommentsByPostIdOrderByNumberOfLikes(userId, postId, pageable)
                .map(comment -> commentToCommentDTO(comment));
    }
}
