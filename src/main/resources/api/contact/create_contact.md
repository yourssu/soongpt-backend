# createContact (POST /api/contacts)

## Request

### Request Body

| Name      | Type   | Required | Description      |
|-----------|--------|----------|------------------|
| `content` | string | true     | Contact content  |

### Example Request

```json
{
  "content": "안녕하세요. 문의사항이 있습니다."
}
```

## Reply

### Response Body

| Name        | Type   | Nullable | Description                    |
|-------------|--------|----------|--------------------------------|
| `id`        | long   | Yes      | Unique contact identifier      |
| `content`   | string | No       | Contact content                |

### 201 Created

```json
{
  "timestamp": "2025-07-20 15:09:00",
  "result": {
    "id": 1,
    "content": "안녕하세요. 문의사항이 있습니다."
  }
}
```

### 400 Bad Request

```json
{
  "timestamp": "2025-07-20 15:09:00",
  "error": "Validation failed"
}
```