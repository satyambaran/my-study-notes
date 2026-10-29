/*
    Amazon API Gateway:
        allows you to create, publish, maintain, monitor, and secure APIs at any scale.
    AWS Lambda:
        lets you run code in response to events without provisioning or managing servers.
    Integration:
        create a new API in Amazon API Gateway
        Define Resources(url, metadata etc) and Methods
        Get ARN (Amazon Resource Name) of the Lambda function.
        Granting API Gateway permission to invoke the Lambda function(through an IAM role)
        Integrate Lambda with API Gateway:
            Define API Gateway resources and methods, AWS Lambda functions, and other related resources (like IAM roles) in a CloudFormation template
            Use the CloudFormation console, AWS CLI, or SDKs to create a stack from the template
            Update the stack to modify the resources or their configurations
            CloudFormation will handle updates
            Rollback:
                If something goes wrong during stack creation or update, CloudFormation can automatically roll back changes
*/