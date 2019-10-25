package com.linweili.miaosha;

import com.linweili.miaosha.dao.UserDOMapper;
import com.linweili.miaosha.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.linweili.miaosha"})
@RestController
@MapperScan("com.linweili.miaosha.dao")
public class MiaoshaApplication {

	@Autowired
	private UserDOMapper userDOMapper;

	public static void main(String[] args) {
		SpringApplication.run(MiaoshaApplication.class, args);
	}

	@RequestMapping("/")
	public String home(){
		UserDO userDO = userDOMapper.selectByPrimaryKey(1);
		if (userDO == null){
			return "用户对象不存在";
		}else {
			return "用户名称：" + userDO.getName();
		}
	}

}
