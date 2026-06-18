package com.qinhu.oasis.tourism.mapper;

import com.qinhu.oasis.tourism.entity.ParkingSpot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车位数据访问层
 *
 * @author AiKiFan
 * @date 2026-05-15
 */
public interface ParkingSpotMapper {

 /**
 * 根据ID查询车位
 */
 ParkingSpot selectById(@Param("id") Long id);

 /**
 * 查询某区域所有车位
 */
 List<ParkingSpot> selectByZoneId(@Param("zoneId") Long zoneId);

 /**
 * 查询所有超时车位（status=2）
 */
 List<ParkingSpot> selectAllOvertime();

 /**
 * 查询所有已占用车位（status=1）
 */
 List<ParkingSpot> selectAllOccupied();

 /**
 * 更新车位状态及关联信息
 */
 void updateSpot(@Param("id") Long id,
 @Param("status") Integer status,
 @Param("vehicleNo") String vehicleNo,
 @Param("userId") Long userId,
 @Param("orderId") Long orderId,
 @Param("startTime") java.time.LocalDateTime startTime,
 @Param("plannedEndTime") java.time.LocalDateTime plannedEndTime);

 /**
 * 重置车位为空闲
 */
 void resetSpot(@Param("id") Long id);

 /**
 * 统计某区域空闲车位数量
 */
 int countFreeByZone(@Param("zoneId") Long zoneId);

 /**
 * 根据车牌号查询已占用的车位（status=1或2）
 */
 ParkingSpot selectOccupiedByVehicleNo(@Param("vehicleNo") String vehicleNo);
}