package com.enimal.backend.service.Impl;

import com.enimal.backend.dto.Etc.BadgeShowDto;
import com.enimal.backend.dto.User.*;
import com.enimal.backend.entity.*;
import com.enimal.backend.entity.Collection;
import com.enimal.backend.repository.*;
import com.enimal.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private AttendenceRepository attendenceRepository;
    private BoardRepository boardRepository;
    private PuzzleRepository puzzleRepository;
    private CommentRepository commentRepository;
    private MoneyRepository moneyRepository;
    private CollectionRepository collectionRepository;
    private BadgeRepository badgeRepository;
    private EventDayRepository eventDayRepository;
    @Autowired
    UserServiceImpl(PuzzleRepository puzzleRepository,CollectionRepository collectionRepository, BadgeRepository badgeRepository,UserRepository userRepository,MoneyRepository moneyRepository, AttendenceRepository attendenceRepository,BoardRepository boardRepository,CommentRepository commentRepository,EventDayRepository eventDayRepository){
        this.userRepository = userRepository;
        this.moneyRepository = moneyRepository;
        this.collectionRepository = collectionRepository;
        this.badgeRepository = badgeRepository;
        this.attendenceRepository = attendenceRepository;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.puzzleRepository = puzzleRepository;
        this.eventDayRepository = eventDayRepository;
    }

    @Override
    public BadgeShowDto loginUser(UserLoginDto userLoginDto) {
        List<String> modal = new ArrayList<>();
        BadgeShowDto badgeShowDto = new BadgeShowDto();
        Optional<User> user = userRepository.findByWallet(userLoginDto.getWallet());
        int convertDate = LocalDateTime.now().getDayOfYear();
        if(!user.isPresent() && userLoginDto.getId() != null){ // ????????? ???????????? ?????? ????????????
            User userRegist = new User();
            userRegist.setId(userLoginDto.getId());
            userRegist.setNickname(userLoginDto.getId());
            userRegist.setWallet(userLoginDto.getWallet());
            userRegist.setLastPuzzle("lastPuzzle");
            userRepository.save(userRegist);
            badgeShowDto.setUserId(userLoginDto.getId());
        }else if(!user.isPresent() && userLoginDto.getId() == null){
            badgeShowDto.setResult(false);
            badgeShowDto.setUserId(userLoginDto.getId());
            return badgeShowDto;
        }else{
            badgeShowDto.setUserId(user.get().getId());
        }
        user = userRepository.findByWallet(userLoginDto.getWallet());
        Optional<Attendence> attendenceCheck = attendenceRepository.findByUserIdAndConvertdate(user.get().getId(),convertDate);
        if(!attendenceCheck.isPresent()){ // ???????????? ?????? ???????????? ????????????
            Attendence attendence = new Attendence();
            attendence.setUserId(user.get().getId());
            attendence.setAttenddate(LocalDateTime.now().plusHours(9));
            attendence.setConvertdate(LocalDateTime.now().getDayOfYear());
            attendenceRepository.save(attendence);
            // ????????? SAVE?????? ??????
            int save = user.get().getCredit() + 500;
            user.get().setCredit(save);
            userRepository.save(user.get());
            // ?????? 12??? : ?????? ????????? ??????
            String todayDay = attendence.getAttenddate().toString().substring(5,10);
            Optional<Badge> isBadge = badgeRepository.findByUserIdAndBadge(user.get().getId(),"?????? ?????????");
            Optional<EventDay> eventDay = eventDayRepository.findByEventDate(todayDay);
            if(!isBadge.isPresent()&&eventDay.isPresent()){
                Badge badge = new Badge();
                badge.setBadge("?????? ?????????");
                badge.setCreatedate(LocalDateTime.now().plusHours(9));
                badge.setUser(user.get());
                badge.setPercentage(2);
                badgeRepository.save(badge);
                modal.add(badge.getBadge());
                badgeShowDto.setEventDayName(eventDay.get().getEventName());
            }
        }
        // ?????? 6??? : ????????? ?????? ???????????? ??? ?????? -> ????????? ???????????? ?????? ???????????? ???????????????
        List<Attendence> attendences = attendenceRepository.findByUserIdOrderByConvertdateDesc(user.get().getId());
        // ????????? ?????? : size 7??????
        int size = attendences.size();
        if(size >= 7){
            if((attendences.get(0).getConvertdate() - attendences.get(6).getConvertdate()) == 6) {
                Optional<Badge> realBadge = badgeRepository.findByUserIdAndBadge(user.get().getId(), "?????????");
                if(!realBadge.isPresent()){ // ???????????? ????????? ??????
                    Badge badge = new Badge();
                    badge.setBadge("?????????");
                    badge.setCreatedate(LocalDateTime.now().plusHours(9));
                    badge.setUser(user.get());
                    badge.setPercentage(2);
                    badgeRepository.save(badge);
                    modal.add(badge.getBadge());
                }
            }
        }
        // ?????? ??????
        String[] arr = new String[modal.size()];
        for(int i=0; i< modal.size(); i++){
            arr[i] = modal.get(i);
        }
        badgeShowDto.setModalName(arr);
        badgeShowDto.setResult(true);
        return badgeShowDto;
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public void updateUser(String userId, String userNickname) {
        Optional<User> user = userRepository.findById(userId);
        user.get().setNickname(userNickname);

        userRepository.save(user.get());
    }

    @Override
    public Map<Integer, LocalDateTime> attendUser(String userId) {
        Map<Integer, LocalDateTime> result = new HashMap<>();
        List<Attendence> attendences = attendenceRepository.findByUserId(userId);
        for(int i =0 ;i<attendences.size();i++){
            result.put(i+1,attendences.get(i).getAttenddate());
        }
        return result;
    }

    @Override
    public List<UserPostListDto> boardList(String userId,Integer pageSize,Integer lastIdx) {
        List<UserPostListDto> userPostListDtos = new ArrayList<>();
        Pageable pageable = PageRequest.ofSize(pageSize);
        if(lastIdx == 0){
            lastIdx = boardRepository.findTop1ByOrderByIdxDesc().get().getIdx() +1;
        }
        Slice<Board> boards = boardRepository.findByUserIdOrderByIdxDesc(userId,lastIdx,pageable);
        for(Board board : boards){
            UserPostListDto userPostListDto = new UserPostListDto();
            userPostListDto.setIdx(board.getIdx());
            userPostListDto.setTitle(board.getTitle());
            userPostListDto.setContent(board.getContent());
            userPostListDto.setPicture(board.getPicture());
            userPostListDto.setCreateDate(board.getCreatedate());
            userPostListDto.setModifyDate(board.getModifydate());
            userPostListDto.setView(board.getView());
            userPostListDtos.add(userPostListDto);
        }
        return userPostListDtos;
    }

    @Override
    public List<UserCommentListDto> listCommentUser(String userId,Integer pageSize,Integer lastIdx) {
        List<UserCommentListDto> userCommentListDtos = new ArrayList<>();
        Pageable pageable = PageRequest.ofSize(pageSize);
        if(lastIdx == 0){
            lastIdx = commentRepository.findTop1ByOrderByIdxDesc().get().getIdx() +1;
        }
        Slice<Comment> comments = commentRepository.findByUserIdOrderByIdxDesc(userId,lastIdx,pageable);
        for(Comment comment : comments){
            UserCommentListDto userCommentListDto = new UserCommentListDto();
            userCommentListDto.setBoardIdx(comment.getBoard().getIdx());
            userCommentListDto.setBoardTitle(comment.getBoard().getTitle());
            userCommentListDto.setContent(comment.getContent());
            userCommentListDto.setCreateDate(comment.getCreatedate());
            userCommentListDtos.add(userCommentListDto);
        }
        return userCommentListDtos;
    }

    @Override
    public List<UserMoneyListDto> listMoneyUser(String userId) {
        List<UserMoneyListDto> userMoneyListDtos = new ArrayList<>();
        List<Money> monies = moneyRepository.findByUserId(userId);
        for(Money money : monies){
            UserMoneyListDto userMoneyListDto = new UserMoneyListDto();
            userMoneyListDto.setUseCredit(money.getCredit());
            userMoneyListDto.setCreateDate(money.getCreatedate());
            userMoneyListDto.setDonateCredit(money.getDonateCredit());
            userMoneyListDtos.add(userMoneyListDto);
        }

        return userMoneyListDtos;
    }

    @Override
    public UserProfileDto profileUser(String nickname) {
        UserProfileDto userProfileDto = new UserProfileDto();
        Optional<User> user = userRepository.findByNickname(nickname);
        if(user.isPresent()){
            String userId = user.get().getId();
            Integer donationRank = userRepository.findByUserIdRank(userId); // ?????? ?????? ?????? ??????
            Integer collectionCount = collectionRepository.countByUserId(userId); //?????? ????????? ????????? ???
            Integer collectionRank = collectionRepository.findByUserIdRank(userId); //?????? ?????? ????????? ??????
            if(collectionRank == null) collectionRank = Math.toIntExact(userRepository.count());
            List<Badge> badges = badgeRepository.findByUserId(userId);
            List<UserBadgeListDto> userBadgeListDtos = new ArrayList<>();
            for(Badge badge:badges){
                UserBadgeListDto userBadgeListDto = new UserBadgeListDto();
                userBadgeListDto.setNickname(badge.getUser().getNickname());
                userBadgeListDto.setBadge(badge.getBadge());
                userBadgeListDto.setCreatedate(badge.getCreatedate());
                userBadgeListDto.setPercentage(badge.getPercentage());
                userBadgeListDtos.add(userBadgeListDto);
            }
            userProfileDto.setNickname(user.get().getNickname());
            userProfileDto.setCollectionCount(collectionCount);
            userProfileDto.setCollectionRank(collectionRank);
            userProfileDto.setDonationRank(donationRank);
            userProfileDto.setUsedCount(user.get().getUsedcount());
            userProfileDto.setUsedCredit(user.get().getUsedcredit());
            userProfileDto.setBadges(userBadgeListDtos);
        }
        return userProfileDto;
    }

    @Override
    public List<Map<String,Object>> completionUser(String nickname) {
        List<Map<String,Object>> result = new ArrayList<>();
        Optional<User> user = userRepository.findByNickname(nickname);
        if(user.isPresent()){
            List<Collection> collections = collectionRepository.findByUserId(user.get().getId());
            for(Collection collection : collections){
                Map<String,Object> data = new HashMap<>();
                data.put("idx",collection.getIdx());
                data.put("info",collection.isInfo());
                data.put("animal",collection.getAnimal());
                data.put("tokenIdInfo",collection.getTokenIdInfo());
                data.put("nftIdByWallet",nickname);
                data.put("nftType",collection.getNftType());
                data.put("nftURL",collection.getNftURL());
                data.put("nftName",collection.getNftName());
                data.put("createDate",collection.getCreatedate());
                result.add(data);
            }
        }
        return result;
    }

    @Override
    public List<UserCollectionDto> collectionUser(String userId) {
        List<UserCollectionDto> userCollectionDtos = new ArrayList<>();
        List<Puzzle> puzzles = puzzleRepository.findByUserIdOrderByAnimal(userId);
        for(Puzzle puzzle : puzzles){
            UserCollectionDto userCollectionDto = new UserCollectionDto();
            userCollectionDto.setAnimal(puzzle.getAnimal());
            userCollectionDto.setPiece(puzzle.getPiece());
            userCollectionDto.setCount(puzzle.getCount());
            userCollectionDto.setCreateDate(puzzle.getCreatedate());
            userCollectionDtos.add(userCollectionDto);
        }
        return userCollectionDtos;
    }

    @Override
    public boolean checkUser(String nickname) {
        Optional<User> user = userRepository.findByNickname(nickname);
        if(user.isPresent()){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public int currentCredit(String userId) {
        Optional<User> user = userRepository.findById(userId);
        int credit = user.get().getCredit();
        return credit;
    }

    @Override
    public List<UserRankDonationListDto>  rankListDonation(Integer pageSize, Integer lastIdx) {
        List<UserRankDonationListDto> userRankDonationListDtos = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(lastIdx, pageSize, Sort.by(Sort.Order.desc("donation")));
        Slice<User> users = userRepository.findAll(pageRequest);
        for (User user : users) {
            UserRankDonationListDto userRankDonationListDto = new UserRankDonationListDto();
            userRankDonationListDto.setIdx(user.getIdx());
            userRankDonationListDto.setNickname(user.getNickname());
            userRankDonationListDto.setDoantion(user.getDonation());
            userRankDonationListDtos.add(userRankDonationListDto);
        }
        return userRankDonationListDtos;
    }
    @Override
    public List<UserRankCollectionListDto>  rankListCollection(Integer pageSize, Integer lastIdx) {
        List<UserRankCollectionListDto> userRankCollectionListDtos = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(lastIdx, pageSize);
        Slice<UserRankCollectionListDto> collections = collectionRepository.findAllByOrderByIdxDesc( pageRequest);
        for(UserRankCollectionListDto userRankCollectionListDto : collections){
            UserRankCollectionListDto user = new UserRankCollectionListDto();
            Optional<User> userBadge = userRepository.findById(userRankCollectionListDto.getNickname());
            Optional<Badge> badges = badgeRepository.findByUserIdAndBadge(userRankCollectionListDto.getNickname(), "?????? ??? ??????");
            user.setIdx(userRankCollectionListDto.getIdx());
            user.setNickname(userRankCollectionListDto.getNickname());
            user.setCollectionCount(userRankCollectionListDto.getCollectionCount());
            user.setDrawCount(userBadge.get().getUsedcount());
            userRankCollectionListDtos.add(user);
            if(!badges.isPresent()){
                Badge badge = new Badge();
                badge.setBadge("?????? ??? ??????");
                badge.setCreatedate(LocalDateTime.now().plusHours(9));
                badge.setUser(userBadge.get());
                badge.setPercentage(2);
                badgeRepository.save(badge);
            }
        }
        return userRankCollectionListDtos;
    }
}
