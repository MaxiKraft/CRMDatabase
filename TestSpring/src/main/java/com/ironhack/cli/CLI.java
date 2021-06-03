package com.ironhack.cli;


import com.ironhack.crm.*;
import com.ironhack.reports.ReportsSalesRep;
import com.ironhack.repository.AccountRepository;
import com.ironhack.repository.*;

import javax.transaction.Transactional;
import java.util.*;

import static com.ironhack.utils.Utils.objectListToString;
import static com.ironhack.utils.Utils.oppertunityListToString;


public class CLI {
    private static LeadRepository leadRepository;
    private static ContactRepository contactRepository;
    private static OpportunityRepository opportunityRepository;
    private static AccountRepository accountRepository;
    private static SalesRepRepository salesRepRepository;


    public static Map<Integer, Lead> leadMap = new HashMap<>();
    public static Map<Integer, Opportunity> opportunityMap = new HashMap<>();
    public static List<Account> acctList = new ArrayList<>();
    public static Scanner scan = new Scanner(System.in);

    public static void initRepository(LeadRepository leadRepository, ContactRepository contactRepository, OpportunityRepository opportunityRepository, AccountRepository accountRepository, SalesRepRepository salesRepRepository) {
        CLI.leadRepository = leadRepository;
        CLI.contactRepository = contactRepository;
        CLI.opportunityRepository = opportunityRepository;
        CLI.accountRepository = accountRepository;
        CLI.salesRepRepository = salesRepRepository;
    }

    public static void start() {
        System.out.println("Welcome to the CRM application.");
        mainMenu();
    }

    public static void mainMenu() {
        System.out.print("Please enter a command, or help to see a list of commands: ");
        String input = scan.nextLine().toLowerCase();
        handleInput(input);
    }

    public static void handleInput(String input) {
        String[] inputArgs = input.split(" ");
        String command = inputArgs[0];
        switch (command) {
            case "help":
                displayHelp();
                break;
            case "new":
                createNewLead(inputArgs);
                break;
            case "create":
                createNewSalesrep(inputArgs);
                break;
            case "show":
                showAllLeads(inputArgs);
                break;
            case "lookup":
                leadDetailsById(inputArgs);
                break;
            case "convert":
                convertLead(inputArgs);
                break;
            case "close-lost":
            case "close-won":
                changeOppStatus(inputArgs);
                break;
            case "list":
                showAllSalesReps(inputArgs);
                break;
            case "print-reports":
                printReports(inputArgs);
                break;
            case "exit":
                System.exit(0);
                break;
            default:
                invalidCommand();
        }
    }


    public static void invalidCommand() {
        System.out.println("Command invalid.");
        mainMenu();
    }

    public static void displayHelp() {
        System.out.println("-------------------------------------");
        System.out.println("Valid commands are as follows:");
        System.out.println("");
        System.out.println("help                Displays this help menu");
        System.out.println("print-reports       Displays all reports");
        System.out.println("create salesrep     Enters a prompt screen to create a new salesrep");
        System.out.println("list salesreps      Display all salesreps");
        System.out.println("new lead            Enters a prompt screen to create a new lead");
        System.out.println("show leads          Displays all leads currently in the system");
        System.out.println("lookup lead <id>    Displays lead details for the given id");
        System.out.println("convert <id>        Converts a lead to an opportunity for the given id");
        System.out.println("close-lost <id>     Changes the status to CLOSED_LOST for the given opportunity id");
        System.out.println("close-won <id>      Changes the status to CLOSED_WON for the given opportunity id");
        System.out.println("exit                Exits the program");
        System.out.println("-------------------------------------");
        mainMenu();
    }

    public static void createNewLead(String[] args) {
        if (args.length == 1 || !args[1].equals("lead")) {
            invalidCommand();
            return;
        }

        Lead lead = new Lead();


        System.out.println("Enter the lead information.");
        System.out.print("Name: ");
        lead.setName(scan.nextLine());

        System.out.print("Phone Number: ");
        lead.setPhoneNumber(scan.nextLine());

        System.out.print("Email: ");
        lead.setEmail(scan.nextLine());

        System.out.print("Company Name: ");
        lead.setCompanyName(scan.nextLine());

        setSalesRepInLead(lead);

        System.out.println("-------------------------------------");
        mainMenu();
    }

    public static void createNewSalesrep(String[] args) {
        if (args.length == 1 || !args[1].equals("salesrep")) {
            invalidCommand();
            return;
        }

        SalesRep salesRep = new SalesRep();


        System.out.println("Enter the salesrep information.");
        System.out.print("Name: ");
        salesRep.setName(scan.nextLine());

        salesRepRepository.save(salesRep); //insert object to salesrepRepository here

        System.out.println("-------------------------------------");
        mainMenu();
    }


    public static void showAllLeads(String[] args) {
        if (args.length == 1 || !args[1].equals("leads")) {
            invalidCommand();
            return;
        }
        System.out.println("------------------------");
        System.out.println(CLI.leadRepository.findAllLeads().toString());
        System.out.println("------------------------");
        mainMenu();
    }

    private static void showAllSalesReps(String[] inputArgs) {
        if (inputArgs.length == 1 || !inputArgs[1].equals("salesreps")) {
            invalidCommand();
            return;
        }
        System.out.println("------------------------");
        System.out.println(CLI.salesRepRepository.findAllSalesRep().toString());
        System.out.println("------------------------");
        mainMenu();
    }


    @Transactional
    public static void leadDetailsById(String[] args) {
        if (args.length == 1 || !args[1].equals("lead")) {
            invalidCommand();
            return;
        }

        try {
            Integer id = Integer.parseInt(args[2]);
            Lead lead = CLI.leadRepository.getById(id);
            if (lead.getName() != null) {
                System.out.println("ID: " + lead.getId());
                System.out.println("Name: " + lead.getName());
                System.out.println("Company: " + lead.getCompanyName());
                System.out.println("Phone: " + lead.getPhoneNumber());
                System.out.println("Email: " + lead.getEmail());
                System.out.println("------------------------");
            }
            mainMenu();
        } catch (Exception e) {
            System.out.println("ID not found");
            // show all leads
            mainMenu();
        }
    }

    public static Opportunity createOpportunity() {
        Opportunity opp = new Opportunity();

        System.out.println("Enter the opportunity information.");
        System.out.print("Product (Hybrid, Flatbed, or Box): ");
        while (opp.getProduct() == null) {
            try {
                opp.setProduct(Product.valueOf(scan.nextLine().toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid selection.");
                System.out.print("Enter only Hybrid, Flatbed, or Box: ");
            }
        }

        System.out.print("Quantity: ");
        while (opp.getQuantity() <= 0) {
            try {
                opp.setQuantity(Integer.parseInt(scan.nextLine()));
                if (opp.getQuantity() <= 0)
                    throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid entry.");
                System.out.print("Enter a positive integer value: ");
            }
        }

        opp.setStatus(Status.OPEN);
        System.out.println("-------------------------------------");
        return opp;
    }

    public static Account createAccount() {
        Account acct = new Account();

        System.out.println("Enter the account information.");
        System.out.print("Industry: ");
        while (acct.getIndustry() == null) {
            try {
                acct.setIndustry(Industry.valueOf(scan.nextLine().toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid selection.");
                System.out.print("Enter only Produce, Ecommerce, Manufacturing, Medical, or Other: ");
            }
        }

        System.out.print("Employee Count: ");
        while (acct.getEmployeeCount() <= 0) {
            try {
                acct.setEmployeeCount(Integer.parseInt(scan.nextLine()));
                if (acct.getEmployeeCount() <= 0)
                    throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid entry.");
                System.out.print("Enter a positive integer value: ");
            }
        }

        System.out.print("City: ");
        acct.setCity(scan.nextLine());

        System.out.print("Country: ");
        acct.setCountry(scan.nextLine());

        System.out.println("-------------------------------------");
        return acct;
    }


    @Transactional
    public static void convertLead(String[] args) {

        if (args.length == 1) {
            invalidCommand();
            return;
        }

        try {
            Integer id = Integer.parseInt(args[1]);
            Lead lead = CLI.leadRepository.findById(id).get();
            Contact newContact = new Contact(lead);
            Opportunity newOpp = createOpportunity();
            newOpp.setSalesRep(lead.getSalesRep());
            newOpp.setDecisionMaker(newContact);
            Boolean answerRequired = false;
            Account acct = null;
            while (answerRequired == false) {
                System.out.println("Do you like to create a new Account? (Y/N)");
                String answer = scan.nextLine().toUpperCase(Locale.ROOT);
                switch (answer) {
                    case "Y":
                        System.out.println("You Choose Yes");
                        acct = createAccount();
                        accountRepository.save(acct);
                        newOpp.setAccount(acct);
                        answerRequired = true;
                        break;
                    case "N":
                        System.out.println("Please Enter the Account ID to which u want to add the Opportunity:");
                        Integer accId = Integer.parseInt(scan.nextLine());
                        try {
                            acct = accountRepository.findById(accId).get();
                            newOpp.setAccount(acct);
                            answerRequired = true;
                            break;
                        } catch (Exception e) {
                            System.out.println("There is no Account under this ID");
                            System.out.println("Do you want to continue enter Yes!");
                            if (scan.nextLine().toUpperCase(Locale.ROOT).equals("YES")) {
                                answerRequired = false;
                            } else {
                                mainMenu();
                                break;
                            }
                        }
                    default:
                        System.out.println("Please Enter Y or N");
                }
            }
            CLI.contactRepository.save(newContact);
            opportunityRepository.save(newOpp);
            leadRepository.deleteById(id);
            mainMenu();
        } catch (Exception e) {
            System.out.println("ID not found");
            mainMenu();
        }
    }

    public static void setSalesRepInLead(Lead lead) {
        Lead l1 = lead;
        Boolean needSalesRep = false;
        while (needSalesRep != true) {
            try {
                System.out.print("SalesRep Id: ");
                CLI.setSalesRepforLead(scan.nextLine(), l1, salesRepRepository, leadRepository);
                needSalesRep = true;
                break;
            } catch (Exception e) {
                System.out.println("There is no SalesRep under this ID");
                System.out.println("Do you want to continue enter Yes!");
                if (scan.nextLine().toUpperCase(Locale.ROOT).equals("YES")) {
                    needSalesRep = false;
                } else {
                    mainMenu();
                    break;
                }
            }
        }
        CLI.leadRepository.save(l1);
    }

    public static void changeOppStatus(String[] args) {
        if (args.length == 1) {
            invalidCommand();
            return;
        }
        try {
            Integer id = Integer.parseInt(args[1]);
            Opportunity opp = opportunityRepository.getById(id);
            // changes close-lost or close-won to CLOSED_LOST or CLOSED_WON respectively
            opp.setStatus(Status.valueOf(args[0].replace("-", "d_").toUpperCase()));
            opportunityRepository.save(opp);
            mainMenu();
        } catch (Exception e) {
            System.out.println("ID not found");
            mainMenu();
        }
    }

    private static void printReports(String[] inputArgs) {
        //--Reports Salesrep
        System.out.println("Number of leads by Salesreps:" + "\n");
        List<Object[]> objList = CLI.salesRepRepository.getCountLeadsBySalesRep();
        System.out.println(objectListToString(objList));
        System.out.println("Number of Opportunities by Salesreps:" + "\n");
        List<Object[]> objList1 = CLI.salesRepRepository.getCountOpportunitiesBySalesRep();
        System.out.println(objectListToString(objList));
        System.out.println("Number of OPEN Opportunities by Salesreps:" + "\n");
        List<Object[]> objList2 = CLI.salesRepRepository.getCountOpportunitiesByStatusAndBySalesRep("OPEN");
        System.out.println(objectListToString(objList));
        System.out.println("Number of WON Opportunities by Salesreps" + "\n");
        List<Object[]> objList3 = CLI.salesRepRepository.getCountOpportunitiesByStatusAndBySalesRep("CLOSED_WON");
        System.out.println(objectListToString(objList));
        System.out.println("All LOST Opportunities by Salesrps" + "\n");
        List<Object[]> objList4 = CLI.salesRepRepository.getCountOpportunitiesByStatusAndBySalesRep("CLOSED_LOST");
        System.out.println(objectListToString(objList));

        //---Product
        System.out.println("All Opportunities by Product");
        List<Object[]> objList5 = CLI.opportunityRepository.getOpportunityByProduct();
        System.out.println(objectListToString(objList));
        System.out.println("All WON Opportunities");
        List<Opportunity> objList6 = CLI.opportunityRepository.findAllByStatusWon();
        System.out.println(oppertunityListToString(objList6));
        System.out.println("All LOST Opportunities");
        List<Opportunity> objList7 = CLI.opportunityRepository.findAllByStatusLost();
        System.out.println(oppertunityListToString(objList7));
        System.out.println("All OPEN Opportunities");
        List<Opportunity> objList8 = CLI.opportunityRepository.findAllByStatusOpen();
        System.out.println(oppertunityListToString(objList8));

        //--Country
        System.out.println("All Opportunities by Country");
        List<Object[]> objList9 = CLI.opportunityRepository.getOpportunityByCountry();
        System.out.println(objectListToString(objList9));
        System.out.println("All WON Opportunities by Country");
        List<Object[]> objList10 = CLI.opportunityRepository.getCountOpportunitiesByStatusAndByCountry("CLOSED_WON");
        System.out.println(objectListToString(objList10));
        System.out.println("All LOST Opportunities by Country");
        List<Object[]> objList11 = CLI.opportunityRepository.getCountOpportunitiesByStatusAndByCountry("CLOSED_LOST");
        System.out.println(objectListToString(objList11));

        //--City

        //--Industry

        //--EmployeeCount

        //--Quantity

        //-- Opportunities

        mainMenu();
    }
    //Testbaren Methoden

    public static void setSalesRepforLead(String input, Lead lead, SalesRepRepository salesRepRepository, LeadRepository leadRepository) {

        try {

            lead.setSalesRep(salesRepRepository.getById(Integer.parseInt(input)));
            leadRepository.save(lead);
        } catch (IllegalStateException e) {
            System.out.println("SalesRep not found");
        }
    }


}
