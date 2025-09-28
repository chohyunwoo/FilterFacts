# Simple CI/CD Test
- 간단한 백앤드 프로젝트 배포 테스트 입니다.


# NHN Container Registry(NCR)
- Docker 컨테이너 이미지를 쉽고 안전하게 저장, 관리하고 배포할 수 있는 컨테이너 레지스트리 서비스
- 레지스트리 생성

# UserAccessKey
- https://console.nhncloud.com/security-setting 혹은 아이디 오른쪽 클릭, API 보안설정으로 Key,PWD 생성
- UserAccessKey, PWD 생성

# GitHub Secrets 설정
- GitHub Action을 위한 정보 설정
- 중요 정보는 Secrets에서 관리
- NCR_PASSWORD
- NCR_REGISTRY
- NCR_REPOSITORY
- NCR_USERNAME

# 코드 Push -> GitHub Action & NCR 확인
- 코드 Push 이후 GitHub Action 정상 작동 확인 후 NCR 이미지 업로드 확인
<img width="860" height="269" alt="스크린샷 2025-09-22 오후 12 29 26" src="https://github.com/user-attachments/assets/50e00a0c-cc82-4b49-ab1f-36d12398e434" />

  

# NHN Kubernetes Service(NKS)
- 관리형 쿠버네티스 서비스

1. 클러스터 생성
- 키페어 생성시 키페어 다운로드하고 관리 필요(필요시)
- 기본 설정에서 변경 X
- 생성 완료되면 kubeconfig 파일를 활용하여 접속 가능 ( Local에 Docker 관련 설치 필요 ) -> 깃허브에 등록 필요
```bash
kubectl --kubeconfig /Users/crlee/dev/crlee/project/nhnConfig/simple-nks_kubeconfig.yaml get nodes
```

2. NHN Cloud Container Registry(NCR) 서비스 연동
- NCR
```
kubectl create secret docker-registry registry-credential --docker-server={사용자 레지스트리 주소} --docker-username={NHN Cloud 계정 email 주소} --docker-password={서비스 Appkey 또는 통합 Appkey}
```

3. deploy.yaml, service.yaml 생성
- deploy, service, pv, pvc등 필요에 따라 생성 필요
- Helm차트를 사용해도 괜찮음
```bash
kubectl --kubeconfig /Users/crlee/dev/crlee/project/nhnConfig/simple-nks_kubeconfig.yaml apply -f deploy.yaml
kubectl --kubeconfig /Users/crlee/dev/crlee/project/nhnConfig/simple-nks_kubeconfig.yaml apply -f service.yaml
```
<img width="1633" height="219" alt="스크린샷 2025-09-22 오후 12 29 50" src="https://github.com/user-attachments/assets/d914d4c9-3f95-489f-8b0e-eab9ad3278ef" />


4. 서비스 공인 IP 확인 및 접근 테스트
```
http://123.456.789.10/hellow?name=123
{
  "message": "Hellow 123 From Spring Boot"
}
```
<img width="439" height="147" alt="스크린샷 2025-09-22 오후 12 30 20" src="https://github.com/user-attachments/assets/a497134a-90e7-4e67-94ab-2787a2722a80" />


5. 재배포 명령어
- CI/CD Workflow에 추가, ArgoCD, Jenkins등 다양한 방법으로 재배포 설정 가능
```
kubectl --kubeconfig /Users/crlee/dev/crlee/project/nhnConfig/simple-nks_kubeconfig.yaml rollout restart deployment/backend-deployment
```
