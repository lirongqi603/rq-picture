package com.rq.cloudpicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.rq.cloudpicturebackend.model.entity.Picture;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片详情 VO
 *
 * @author lirongqi
 * @date 2026-04-03
 */
@Data
public class PictureVo implements Serializable {

    private static final long serialVersionUID = -8179613285969060245L;

    /**
     * id
     */
    @ApiModelProperty(value = "id", position = 1)
    private Long id;

    /**
     * 图片 url
     */
    @ApiModelProperty(value = "图片 url", position = 2)
    private String url;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称", position = 3)
    private String name;

    /**
     * 简介
     */
    @ApiModelProperty(value = "简介", position = 4)
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", position = 5)
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @ApiModelProperty(value = "标签", position = 6)
    private List<String> tags;

    /**
     * 图片体积
     */
    @ApiModelProperty(value = "图片体积", position = 7)
    private Long picSize;

    /**
     * 图片宽度
     */
    @ApiModelProperty(value = "图片宽度", position = 8)
    private Integer picWidth;

    /**
     * 图片高度
     */
    @ApiModelProperty(value = "图片高度", position = 9)
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    @ApiModelProperty(value = "图片宽高比例", position = 10)
    private Double picScale;

    /**
     * 图片格式
     */
    @ApiModelProperty(value = "图片格式", position = 11)
    private String picFormat;

    /**
     * 上传用户信息
     */
    @ApiModelProperty(value = "上传用户信息", position = 12)
    private UserInfoVo user;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", position = 13)
    private Date createTime;

    /**
     * 编辑时间
     */
    @ApiModelProperty(value = "编辑时间", position = 14)
    private Date editTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", position = 15)
    private Date updateTime;

    /**
     * 封装类转对象
     */
    public static Picture voToObj(PictureVo pictureVo) {
        if (pictureVo == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVo, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureVo.getTags()));
        return picture;
    }

    /**
     * 对象转封装类型
     */
    public static PictureVo objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVo pictureVo = new PictureVo();
        BeanUtil.copyProperties(picture, pictureVo);
        pictureVo.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVo;
    }

}