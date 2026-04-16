package com.point.pointaicodemother.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.point.pointaicodemother.model.entity.User;
import com.point.pointaicodemother.mapper.UserMapper;
import com.point.pointaicodemother.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/PointWu">PointWu</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
