#Account Management System
This is a simple account management system implemented in Java using Spring Boot. 
It allows you to create accounts, retrieve account information, and transfer money between accounts

#Project Structure
1. com.dws.challenge.web.AccountsController
This is the REST controller responsible for handling HTTP requests related to account management. It provides the following endpoints:

POST /v1/accounts: Create a new account.
GET /v1/accounts/{accountId}: Retrieve account information by account ID.
POST /v1/accounts/transfer: Transfer money between two accounts.

2. com.dws.challenge.service.AccountsService
This service class contains business logic for account management. It includes methods for creating accounts and transferring money between accounts.

3. com.dws.challenge.service.NotificationService
This interface defines a notification service used to notify account holders about transfers. An implementation of this service, EmailNotificationService, is also provided.

4. com.dws.challenge.repository.AccountsRepository
This interface defines a repository for managing accounts. An in-memory implementation, AccountsRepositoryInMemory, is provided.

5. com.dws.challenge.domain
This package contains domain objects used in the application, including:

Account: Represents an account with an account ID and balance.
MoneyTransferRequest: Represents a request to transfer money between accounts.
TransferResponse: Represents a response for a money transfer operation.
Usage
You can interact with the account management system through the defined REST endpoints. Here's a brief overview of how to use them:

#Create an Account
To create a new account, send a POST request to /v1/accounts with a JSON body containing the account details:

{
  "accountId": "your_account_id",
  "balance": 1000.00
}
Retrieve Account Information
To retrieve account information, send a GET request to /v1/accounts/{accountId} where {accountId} is the ID of the account you want to retrieve.

Transfer Money
To transfer money between accounts, send a POST request to /v1/accounts/transfer with a JSON body containing the transfer details:
{
  "accountFromId": "source_account_id",
  "accountToId": "target_account_id",
  "amount": 500.00
}
This will transfer $500.00 from the source account to the target account.

Exception Handling
The application handles exceptions such as duplicate account IDs, invalid account IDs, insufficient funds, and negative transfer amounts. It returns appropriate HTTP status codes and error messages for each scenario.

#Test cases
Junit test cases has also been written for controller layer and service layer.

#Conclusion
This simple account management system demonstrates the basic principles of building a RESTful API using Spring Boot. It allows you to create accounts, retrieve account information, and transfer money between accounts while handling various exceptions and providing appropriate responses.
