package org.example.col_stu_ptj_sys.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.ApiResponse;
import org.example.col_stu_ptj_sys.dto.CreateCreditDisputeRequest;
import org.example.col_stu_ptj_sys.dto.CreditDisputeSimpleVO;
import org.example.col_stu_ptj_sys.dto.DisputeCounterpartyVO;
import org.example.col_stu_ptj_sys.dto.DisputeRelationPickVO;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.entity.CreditAdjustLog;
import org.example.col_stu_ptj_sys.entity.UserCredit;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.service.UserCreditPortalService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Tag(name = "学生端-信用", description = "信用档案、调整记录、争议工单")
@RestController
@RequestMapping("/api/student/credit")
@RequiredArgsConstructor
public class StudentCreditController {

    private final UserCreditPortalService userCreditPortalService;

    @Operation(summary = "我的信用档案")
    @GetMapping
    public ResponseEntity<ApiResponse<UserCredit>> myCredit() {
        return ResponseEntity.ok(ApiResponse.success(userCreditPortalService.myCredit(UserRole.STUDENT)));
    }

    @Operation(summary = "信用调整记录")
    @GetMapping("/adjust-logs")
    public ResponseEntity<ApiResponse<PageResponse<CreditAdjustLog>>> adjustLogs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseEntity.ok(ApiResponse.success(
                userCreditPortalService.myAdjustLogs(UserRole.STUDENT, current, size)));
    }

    @Operation(summary = "争议对象候选项（来自历史投递的企业）")
    @GetMapping("/dispute-counterparties")
    public ResponseEntity<ApiResponse<List<DisputeCounterpartyVO>>> disputeCounterparties() {
        return ResponseEntity.ok(ApiResponse.success(
                userCreditPortalService.listDisputeCounterparties(UserRole.STUDENT)));
    }

    @Operation(summary = "关联业务记录候选项（需先选对方用户）")
    @GetMapping("/dispute-relation-picks")
    public ResponseEntity<ApiResponse<List<DisputeRelationPickVO>>> disputeRelationPicks(
            @RequestParam long targetUserId,
            @RequestParam String relatedType) {
        return ResponseEntity.ok(ApiResponse.success(
                userCreditPortalService.listDisputeRelationPicks(UserRole.STUDENT, targetUserId, relatedType)));
    }

    @Operation(summary = "争议工单（scope=INITIATED 我发起 / RECEIVED 针对我 / ALL 全部）")
    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<PageResponse<CreditDisputeSimpleVO>>> disputes(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(defaultValue = "INITIATED") String scope) {
        return ResponseEntity.ok(ApiResponse.success(
                userCreditPortalService.myDisputes(UserRole.STUDENT, current, size, scope)));
    }

    @Operation(summary = "发起信用争议")
    @PostMapping("/disputes")
    public ResponseEntity<ApiResponse<Void>> submitDispute(@Valid @RequestBody CreateCreditDisputeRequest req) {
        userCreditPortalService.submitDispute(UserRole.STUDENT, req);
        return ResponseEntity.ok(ApiResponse.success("已提交，请等待管理员处理", null));
    }

    @Operation(summary = "补充争议材料（待处理阶段，申诉人可多次提交）")
    @PostMapping(value = "/disputes/{id}/supplement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> supplement(
            @PathVariable Long id,
            @RequestParam(required = false) String supplementText,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        List<MultipartFile> list = files == null ? List.of() : Arrays.asList(files);
        userCreditPortalService.submitDisputeSupplement(UserRole.STUDENT, id, supplementText, list);
        return ResponseEntity.ok(ApiResponse.success("已保存补充材料", null));
    }
}
