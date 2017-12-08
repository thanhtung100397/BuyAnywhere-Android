package com.uides.buyanywhere.service.shop;

import com.uides.buyanywhere.model.Order;
import com.uides.buyanywhere.model.PageResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by TranThanhTung on 08/12/2017.
 */

public interface GetShopOrderService {
    @GET("")
    Observable<PageResult<Order>> getShopOrders(@Path("shopId") String shopID,
                                               @Query("pageIndex") int pageIndex,
                                               @Query("pageSize") Integer pageSize,
                                               @Query("orderBy") String field,
                                               @Query("orderType") String type);
}
