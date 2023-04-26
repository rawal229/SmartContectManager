package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contect;
import com.smart.entities.User;

public interface ContectRepo extends JpaRepository<Contect, Integer> {
    // pagination....

    @Query("from Contect as c where c.user.id =:userId")
    // currentPage - page
    // Contact Per Page - 5
    public Page<Contect> findContectByUser(@Param("userId") int userId, Pageable pageable);

    // search
    public List<Contect> findByNameContainingAndUser(String name, User user);

}
