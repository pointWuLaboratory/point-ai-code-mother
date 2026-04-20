package com.point.pointaicodemother.service;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.point.pointaicodemother.model.dto.app.AppQueryRequest;
import com.point.pointaicodemother.model.entity.App;
import com.point.pointaicodemother.model.entity.User;
import com.point.pointaicodemother.model.vo.AppVO;
import com.point.pointaicodemother.model.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/PointWu">PointWu</a>
 */
@Service
public interface AppService extends IService<App> {

    /**
     * 获取应用封装类。
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类列表。
     * @param appList
     * @return
     */

    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构建查询条件。
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 通过对话生成应用代码
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 部署应用
     * @param appId
     * @param loginUser
     * @return 可访问的部署地址
     */
    String deployApp(Long appId, User loginUser);
}
