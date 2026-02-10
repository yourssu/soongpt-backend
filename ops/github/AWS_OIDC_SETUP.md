# GitHub Actions → AWS OIDC 전환 가이드

이 레포의 워크플로(`dev.yml`, `prod.yml`, `deploy-only.yml`)는
`aws-actions/configure-aws-credentials@v4`를 OIDC 방식으로 사용하도록 변경됨.

## 1) AWS에 GitHub OIDC IdP 생성 (최초 1회)

AWS IAM > Identity providers 에서 아래 provider가 없으면 생성:

- Provider URL: `https://token.actions.githubusercontent.com`
- Audience: `sts.amazonaws.com`

## 2) IAM Role 생성 (예: `GitHubActionsSoongptRole`)

### Trust policy 예시

> `ACCOUNT_ID`, `ORG`, `REPO` 값은 실제 값으로 교체

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": [
            "repo:ORG/REPO:environment:dev",
            "repo:ORG/REPO:environment:prod",
            "repo:ORG/REPO:environment:dev-backup"
          ]
        }
      }
    }
  ]
}
```

## 3) Role permission policy 부여

ECR Public push/pull/정리용 최소 예시:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr-public:GetAuthorizationToken",
        "sts:GetServiceBearerToken"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr-public:BatchCheckLayerAvailability",
        "ecr-public:CompleteLayerUpload",
        "ecr-public:DescribeImages",
        "ecr-public:DescribeRepositories",
        "ecr-public:InitiateLayerUpload",
        "ecr-public:PutImage",
        "ecr-public:UploadLayerPart",
        "ecr-public:BatchDeleteImage"
      ],
      "Resource": [
        "arn:aws:ecr-public::ACCOUNT_ID:repository/yourssu/soongpt",
        "arn:aws:ecr-public::ACCOUNT_ID:repository/yourssu/soongpt/rusaint-service"
      ]
    }
  ]
}
```

## 4) GitHub Environment Variable 설정

각 환경(`dev`, `prod`, `dev-backup`)에 동일하게 추가:

- `AWS_ROLE_TO_ASSUME=arn:aws:iam::<ACCOUNT_ID>:role/GitHubActionsSoongptRole`

CLI 예시:

```bash
REPO=yourssu/soongpt-backend
ROLE_ARN='arn:aws:iam::<ACCOUNT_ID>:role/GitHubActionsSoongptRole'

for ENV in dev prod dev-backup; do
  gh variable set AWS_ROLE_TO_ASSUME --repo "$REPO" --env "$ENV" --body "$ROLE_ARN"
done
```

## 5) 기존 static key secret 정리 (선택)

OIDC 정상 동작 확인 후 아래 secret 삭제 가능:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

삭제 예시:

```bash
REPO=yourssu/soongpt-backend
for ENV in dev prod dev-backup; do
  gh secret delete AWS_ACCESS_KEY_ID --repo "$REPO" --env "$ENV" || true
  gh secret delete AWS_SECRET_ACCESS_KEY --repo "$REPO" --env "$ENV" || true
done
```
