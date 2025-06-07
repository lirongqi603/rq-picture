package com.rq.cloudpicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rq.cloudpicturebackend.model.entity.Picture;
import com.rq.cloudpicturebackend.service.PictureService;
import com.rq.cloudpicturebackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
 * 图片服务实现类
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

}




