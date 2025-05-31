package com.cinema.views.admin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.cinema.models.BaoCao;
import com.cinema.services.BaoCaoService;
import com.cinema.utils.DatabaseConnection;

public class DashboardViewTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Mock
    private BaoCaoService mockBaoCaoService;
    
    private DashboardView dashboardView;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Tạo mock connection
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        
        // Tạo một đối tượng DatabaseConnection thật
        mockDbConnection = new DatabaseConnection();
        
        // Sử dụng reflection để thay thế connection thật bằng mock
        Field connectionField = DatabaseConnection.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(mockDbConnection, mockConnection);
        
        // Thiết lập mock
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Tạo một instance của DashboardView với mock DatabaseConnection
        dashboardView = new DashboardView(mockDbConnection);
        
        // Thay thế BaoCaoService thật bằng mock
        mockBaoCaoService = mock(BaoCaoService.class);
        Field baoCaoServiceField = DashboardView.class.getDeclaredField("baoCaoService");
        baoCaoServiceField.setAccessible(true);
        baoCaoServiceField.set(dashboardView, mockBaoCaoService);
    }

    @Test
    public void testLoadRealDataWithLargeDataset() throws Exception {
        // Tạo một danh sách lớn các báo cáo (giả lập dữ liệu lớn)
        List<BaoCao> largeBaoCaoList = createLargeBaoCaoList(1000);
        
        // Thiết lập mock BaoCaoService để trả về danh sách lớn
        when(mockBaoCaoService.getBaoCaoDoanhThuTheoPhim(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(largeBaoCaoList);
        
        // Thiết lập mock cho countTotalCustomers
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(5000); // Giả lập 5000 khách hàng
        
        // Gọi phương thức loadRealData thông qua reflection vì nó là private
        Method loadRealDataMethod = DashboardView.class.getDeclaredMethod("loadRealData");
        loadRealDataMethod.setAccessible(true);
        
        // Đo thời gian thực thi
        long startTime = System.currentTimeMillis();
        loadRealDataMethod.invoke(dashboardView);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("Thời gian tải dữ liệu lớn: " + (endTime - startTime) + "ms");

        // Thêm assertion để kiểm tra thời gian thực thi
        assertTrue(executionTime < 5000, "Thời gian tải dữ liệu phải dưới 5 giây");
        
        // Xác minh rằng các phương thức đã được gọi
        verify(mockBaoCaoService).getBaoCaoDoanhThuTheoPhim(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(mockConnection, atLeastOnce()).prepareStatement(anyString());
    }
    
    @Test
    public void testDataLoadingPerformance() throws Exception {
        // Thiết lập mock ResultSet để trả về nhiều dòng dữ liệu
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(5000);
        
        // Tạo một danh sách lớn các báo cáo
        List<BaoCao> largeBaoCaoList = createLargeBaoCaoList(1000);
        
        // Thiết lập mock BaoCaoService để trả về danh sách lớn
        when(mockBaoCaoService.getBaoCaoDoanhThuTheoPhim(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(largeBaoCaoList);
        
        // Gọi phương thức loadRealData thông qua reflection vì nó là private
        Method loadRealDataMethod = DashboardView.class.getDeclaredMethod("loadRealData");
        loadRealDataMethod.setAccessible(true);
        
        // Đo thời gian thực thi
        long startTime = System.currentTimeMillis();
        loadRealDataMethod.invoke(dashboardView);
        long endTime = System.currentTimeMillis();
        
        System.out.println("Thời gian tải dữ liệu: " + (endTime - startTime) + "ms");
        
        // Xác minh rằng PreparedStatement đã được tạo và thực thi
        verify(mockConnection, atLeastOnce()).prepareStatement(anyString());
        verify(mockPreparedStatement, atLeastOnce()).executeQuery();
    }
    
    @Test
    public void testUpdateChartWithRealDataForLargeDataset() throws Exception {
        // Thiết lập mock ResultSet để trả về nhiều dòng dữ liệu cho biểu đồ
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, true, true, false);
        
        // Thiết lập dữ liệu mẫu cho mỗi ngày
        when(mockResultSet.getDate("ngay")).thenReturn(
            java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(6)),
            java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(5)),
            java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(4)),
            java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(3)),
            java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(2)),
            java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(1)),
            java.sql.Date.valueOf(java.time.LocalDate.now())
        );
        
        // Thiết lập số vé và doanh thu cao cho mỗi ngày
        when(mockResultSet.getInt("soVe")).thenReturn(100, 150, 200, 250, 300, 350, 400);
        when(mockResultSet.getDouble("doanhThu")).thenReturn(
            5000000.0, 7500000.0, 10000000.0, 12500000.0, 15000000.0, 17500000.0, 20000000.0
        );
        
        // Gọi phương thức updateChartWithRealData thông qua reflection
        Method updateChartMethod = DashboardView.class.getDeclaredMethod("updateChartWithRealData");
        updateChartMethod.setAccessible(true);
        
        // Đo thời gian thực thi
        long startTime = System.currentTimeMillis();
        updateChartMethod.invoke(dashboardView);
        long endTime = System.currentTimeMillis();
        
        System.out.println("Thời gian cập nhật biểu đồ: " + (endTime - startTime) + "ms");
        
        // Xác minh rằng PreparedStatement đã được tạo và thực thi
        verify(mockConnection, atLeastOnce()).prepareStatement(anyString());
        verify(mockPreparedStatement, atLeastOnce()).executeQuery();
    }
    
    @Test
    public void testUIPerformanceWithLargeData() throws Exception {
        // Tạo một danh sách lớn các báo cáo
        List<BaoCao> largeBaoCaoList = createLargeBaoCaoList(5000);
        
        // Thiết lập mock BaoCaoService để trả về danh sách lớn
        when(mockBaoCaoService.getBaoCaoDoanhThuTheoPhim(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(largeBaoCaoList);
        
        // Thiết lập mock cho countTotalCustomers
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(10000); // Giả lập 10000 khách hàng
        
        // Gọi phương thức loadRealData
        Method loadRealDataMethod = DashboardView.class.getDeclaredMethod("loadRealData");
        loadRealDataMethod.setAccessible(true);
        
        // Đo thời gian thực thi
        long startTime = System.currentTimeMillis();
        loadRealDataMethod.invoke(dashboardView);
        
        // Đo thời gian vẽ giao diện
        dashboardView.repaint();
        long endTime = System.currentTimeMillis();
        
        System.out.println("Thời gian tải và vẽ giao diện với dữ liệu lớn: " + (endTime - startTime) + "ms");
    }
    
    @Test
    public void testMemoryUsageWithLargeData() throws Exception {
        // Đo lượng bộ nhớ trước khi tải dữ liệu lớn
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Tạo một danh sách rất lớn các báo cáo
        List<BaoCao> veryLargeBaoCaoList = createLargeBaoCaoList(10000);
        
        // Thiết lập mock BaoCaoService để trả về danh sách lớn
        when(mockBaoCaoService.getBaoCaoDoanhThuTheoPhim(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(veryLargeBaoCaoList);
        
        // Thiết lập mock cho countTotalCustomers
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(20000); // Giả lập 20000 khách hàng
        
        // Gọi phương thức loadRealData
        Method loadRealDataMethod = DashboardView.class.getDeclaredMethod("loadRealData");
        loadRealDataMethod.setAccessible(true);
        loadRealDataMethod.invoke(dashboardView);
        
        // Đo lượng bộ nhớ sau khi tải dữ liệu lớn
        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("Bộ nhớ sử dụng trước khi tải: " + memoryBefore / 1024 / 1024 + " MB");
        System.out.println("Bộ nhớ sử dụng sau khi tải: " + memoryAfter / 1024 / 1024 + " MB");
        System.out.println("Bộ nhớ tăng thêm: " + (memoryAfter - memoryBefore) / 1024 / 1024 + " MB");
    }
    
    /**
     * Tạo một danh sách lớn các báo cáo để kiểm tra hiệu suất
     */
    private List<BaoCao> createLargeBaoCaoList(int size) {
        List<BaoCao> baoCaoList = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            BaoCao baoCao = new BaoCao();
            baoCao.setSoVeBanRa(10 + i % 50); // 10-59 vé
            baoCao.setTongDoanhThu(50000 + (i % 100) * 10000); // 50,000đ - 1,050,000đ
            baoCao.setDiemDanhGiaTrungBinh(3.0 + (i % 20) / 10.0); // 3.0-4.9 điểm
            baoCaoList.add(baoCao);
        }
        
        return baoCaoList;
    }
}