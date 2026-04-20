package com.rq.cloudpicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rq.cloudpicturebackend.model.entity.Space;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SpaceVo implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "id", position = 1)
    private Long id;


    /**
     * 空间名称
     */
    @ApiModelProperty(value = "空间名称", position = 2)
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @ApiModelProperty(value = "空间级别：0-普通版 1-专业版 2-旗舰版", position = 3)
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @ApiModelProperty(value = "空间图片的最大总大小", position = 4)
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @ApiModelProperty(value = "空间图片的最大数量", position = 5)
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    @ApiModelProperty(value = "当前空间下图片的总大小", position = 6)
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    @ApiModelProperty(value = "当前空间下的图片数量", position = 7)
    private Long totalCount;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id", position = 8)
    private Long userId;


    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", position = 9)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 编辑时间
     */
    @ApiModelProperty(value = "编辑时间", position = 10)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date editTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", position = 11)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 用户信息
     */
    @ApiModelProperty(value = "用户信息", position = 12)
    private UserInfoVo userVo;

    /**
     * 空间类型：0-私有 1-团队
     */
    @ApiModelProperty(value = "空间类型：0-私有 1-团队", position = 13)
    private Integer spaceType;

    @ApiModelProperty(value = "权限列表", position = 14)
    private List<String> permissionList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    /**
     * 封装类转对象
     */
    public static Space voToObj(SpaceVo spaceVo) {
        if (spaceVo == null) {
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceVo, space);
        return space;
    }

    /**
     * 对象转封装类型
     */
    public static SpaceVo objToVo(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVo spaceVo = new SpaceVo();
        BeanUtil.copyProperties(space, spaceVo);
        return spaceVo;
    }
}