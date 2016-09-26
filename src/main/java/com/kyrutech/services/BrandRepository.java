package com.kyrutech.services;

import com.kyrutech.entities.Brand;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by kdrudy on 9/26/16.
 */
public interface BrandRepository extends PagingAndSortingRepository<Brand, Integer> {
}
