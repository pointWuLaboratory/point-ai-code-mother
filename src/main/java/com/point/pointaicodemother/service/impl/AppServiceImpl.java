package com.point.pointaicodemother.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.point.pointaicodemother.model.entity.App;
import com.point.pointaicodemother.mapper.AppMapper;
import com.point.pointaicodemother.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/PointWu">PointWu</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
