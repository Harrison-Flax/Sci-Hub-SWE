const varChar20 = 20;
const varChar25 = 25;
const varChar50 = 50;
const varChar100 = 100;
const varChar200 = 200;
const varChar255 = 255;
const varChar500 = 500;
const varChar2250 = 2250;
const text = 65535;
const varChar1000 = 1000;
const mediumText = 16777215;

//news
const newsTitleMaxLength = varChar255;
const newsShortDescriptionMaxLength = text;

// register service

const serviceServiceNameMaxLength = varChar255;
const serviceInTitleMaxLength = varChar255;
const serviceDescriptionMaxLength = text;
const servicePurposeMaxLength = text;
const serviceProviderNamesMaxLength = varChar1000;
const serviceProvidersNamesMaxLength = varChar1000;
const serviceProviderAffiliationsMaxLength = varChar1000;
const serviceProvidersAffiliationsMaxLength = varChar1000;
const serviceUrl_frontendMaxLength = varChar1000;
const serviceFrontend_urlMaxLength = varChar1000;
const serviceServiceURLMaxLength = varChar1000;
const serviceHomepageURLMaxLength = varChar1000;
const serviceUrlWithParameterMaxLength = varChar1000;
const serviceBodyMaxLength = text;
const serviceInDescMaxLength  = 10;
const serviceLongDescriptionMaxLength = varChar255;
const serviceLocationMaxLength = 25;
const serviceUlxMaxLength = 25;
const serviceUlyMaxLength = 25;
const serviceLrxMaxLength = 25;
const serviceLryMaxLength = 25;
const serviceDyMaxLength = 25;
const serviceDxMaxLength = 25;
const serviceDateMaxLength = 25;
const serviceDate2MaxLength = 25;
const serviceApplicationLetterBodyMaxLength = text;

// register container
const dockerDockerNameMaxLength = varChar255;
const dockerDescriptionMaxLength  = varChar1000;
const dockerLongDescriptionMaxLength  = mediumText;
const dockerProvidersNamesMaxLength = varChar1000;
const dockerProvidersAffiliationsMaxLength = varChar1000;
const dockerGuiLinkMaxLength = text;
const dockerDockerHubUrlMaxLength = varChar1000;
const dockerRunCommandMaxLength = text;
const dockerDockerRunCommandMaxLength = text;
const dockerProviderTagsMaxLength = varChar255;
const dockerRunProjectMaxLength = varChar255;

// register dataset
const datasetNameMaxLength = varChar255;
const datasetUrlMaxLength = varChar255;
const datasetProviderNamesMaxLength = varChar1000;
const datasetProviderAffiliationsMaxLength = varChar1000;
const datasetTitleMaxLength = varChar255;
const datasetShortDescriptionMaxLength = varChar255;
const datasetPlatformMaxLength = text;
const datasetLocationsMaxLength = varChar255;
const datasetAgencyNameMaxLength = varChar500;
const datasetScienceKeywordsMaxLength = text;
const datasetInstrumentNameMaxLength = varChar255;
const datasetModelMaxLength = varChar500;
const datasetProcessingLevelMaxLength = varChar500;
const datasetLocalFilePathMaxLength = varChar255;
const datasetFrequencyMaxLength = varChar255;
const datasetAlgorithmsMaxLength = varChar255;
const datasetVersionMaxLength = varChar100;
const datasetUnitsMaxLength = varChar255;
const datasetGridDimensionMaxLength = varChar255;
const datasetGridSizeMaxLength = varChar255;
const datasetStatusMaxLength = varChar255;
const datasetResponsiblePersonMaxLength = varChar255;
const datasetCommentMaxLength = varChar255;
const datasetNickNameMaxLength = varChar255;
const datasetHostingLocationMaxLength = varChar255;
const datasetVariablesMaxLength = varChar255;
const datasetTemporalResolutionMaxLength = varChar255;
const datasetSpatialResolutionMaxLength = varChar255;
const datasetServicesMaxLength = varChar1000;
const datasetVariableNamesMaxLength = varChar255;
const datasetSummaryMaxLength = text;
const datasetLongDescriptionMaxLength = text;

// notebook
const notebookNameMaxLength = varChar255;
const notebookDescriptionMaxLength = text;
const notebookProvidersNamesMaxLength = varChar1000;
const notebookProvidersAffiliationsMaxLength = varChar1000;
const notebookHomepageUrlMaxLength = varChar1000;
const notebookNotebookUrlMaxLength = varChar1000;
const notebookLongDescriptionMaxLength = mediumText;

// wishlist
const wishlistWishNameMaxLength = varChar255;
const wishlistBriefDescriptionMaxLength = varChar255;
const wishlistDatasetNameMaxLength = varChar255;
const wishlistRequiredExpertiseMaxLength = varChar255;
const wishlistTimeFrameRequirementsMaxLength = varChar255;
const wishlistLongDescriptionMaxLength = mediumText;

// project
const projectTitleMaxLength = varChar500;
const projectGoalsMaxLength = varChar2250;
const projectLocationMaxLength = varChar200;
const projectTechMaxLength = varChar500;
const projectMemberNameMaxLength = varChar255;
const projectEmailMaxLength = varChar255;
const projectGitMaxLength = varChar500;
const projectTeampageMaxLength = varChar500;
const projectVideoMaxLength = varChar500;
const projectDescriptionMaxLength = mediumText;

//challenge
const challengeDescriptionMaxLength = mediumText;

// user
const userFirstNameMaxLength = varChar255;
const userMiddleInitialMaxLength = varChar255;
const userLastNameMaxLength = varChar255;
const userEmailMaxLength = varChar255;
const userPasswordMaxLength = varChar255;
const userRepasswordMaxLength = varChar255;
const userResearchFieldsMaxLength = varChar255;
const userAffiliationMaxLength = varChar255;
const userTitleMaxLength = varChar255;
const userMailingAddressMaxLength = varChar255;
const userPhoneNumberMaxLength = varChar20;
const userHighestDegreeMaxLength = varChar255;

// post
const postBodyMaxLength = 255;

// bug
const bugTitleMaxLength = varChar255;
const bugDescriptionMaxLength = varChar255;
const bugLongDescriptionMaxLength = mediumText;

// suggestion
const suggestionTitleMaxLength = varChar255;
const suggestionDescriptionMaxLength = varChar255;
const suggestionLongDescriptionMaxLength = mediumText;



/////////////////////////////////////////////////


// // register service
// const serviceServiceNameMaxLength = 10;
// const serviceInTitleMaxLength = 10;
// const serviceDescriptionMaxLength = 10;
// const servicePurposeMaxLength = 10;
// const serviceProviderNamesMaxLength = 10;
// const serviceProvidersNamesMaxLength = 10;
// const serviceProviderAffiliationsMaxLength = 10;
// const serviceInDescMaxLength  = 10;
// const serviceProvidersAffiliationsMaxLength = 10;
// const serviceFrontend_urlMaxLength = 10;
// const serviceServiceURLMaxLength = 10;
// const serviceHomepageURLMaxLength = 10;
// const serviceUrlWithParameterMaxLength = 15;
// const serviceLongDescriptionMaxLength = 10;
// const serviceLocationMaxLength = 10;
// const serviceUlxMaxLength = 10;
// const serviceUlyMaxLength = 10;
// const serviceLrxMaxLength = 10;
// const serviceLryMaxLength = 10;
// const serviceDyMaxLength = 10;
// const serviceDxMaxLength = 10;
// const serviceDateMaxLength = 10;
// const serviceDate2MaxLength = 10;
// const serviceBodyMaxLength = 10;
// const serviceApplicationLetterBodyMaxLength = 10;
//
// // register container
// const dockerDockerNameMaxLength = 10;
// const dockerDescriptionMaxLength  = 10;
// const dockerLongDescriptionMaxLength  = 10;
// const dockerProvidersNamesMaxLength = 10;
// const dockerProvidersAffiliationsMaxLength = 10;
// const dockerGuiLinkMaxLength = 10;
// const dockerDockerHubUrlMaxLength = 10;
// const dockerRunCommandMaxLength = 10;
// const dockerDockerRunCommandMaxLength = 10;
// const dockerInDescMaxLength  = 10;
// const dockerRunProjectMaxLength = 10;
//
//
// // register dataset
// const datasetNameMaxLength = 10;
// const datasetUrlMaxLength = 10;
// const datasetProviderNamesMaxLength = 10;
// const datasetProviderAffiliationsMaxLength = 10;
// const datasetTitleMaxLength = 10;
// const datasetShortDescriptionMaxLength = 10;
// const datasetPlatformMaxLength = 10;
// const datasetLocationsMaxLength = 10;
// const datasetAgencyNameMaxLength = 10;
// const datasetScienceKeywordsMaxLength = 10;
// const datasetInstrumentNameMaxLength = 10;
// const datasetModelMaxLength = 10;
// const datasetProcessingLevelMaxLength = 10;
// const datasetLocalFilePathMaxLength = 10;
// const datasetFrequencyMaxLength = 10;
// const datasetAlgorithmsMaxLength = 10;
// const datasetVersionMaxLength = 10;
// const datasetUnitsMaxLength = 10;
// const datasetGridDimensionMaxLength = 10;
// const datasetGridSizeMaxLength = 10;
// const datasetStatusMaxLength = 10;
// const datasetResponsiblePersonMaxLength = 10;
// const datasetCommentMaxLength = 10;
// const datasetNickNameMaxLength = 10;
// const datasetHostingLocationMaxLength = 10;
// const datasetVariablesMaxLength = 10;
// const datasetTemporalResolutionMaxLength = 10;
// const datasetSpatialResolutionMaxLength = 10;
// const datasetServicesMaxLength = 10;
// const datasetVariableNamesMaxLength = 10;
// const datasetSummaryMaxLength = 10;
// const datasetLongDescriptionMaxLength = 10;
//
// // notebook
// const notebookNameMaxLength = 10;
// const notebookDescriptionMaxLength = 10;
// const notebookProvidersNamesMaxLength = 10;
// const notebookProvidersAffiliationsMaxLength = 10;
// const notebookHomepageUrlMaxLength = 10;
// const notebookGuiUrlMaxLength = 10;
// const notebookLongDescriptionMaxLength = 10;
//
// // wishlist
// const wishlistWishNameMaxLength = 10;
// const wishlistBriefDescriptionMaxLength = 10;
// const wishlistDatasetNameMaxLength = 10;
// const wishlistRequiredExpertiseMaxLength = 10;
// const wishlistTimeFrameRequirementsMaxLength = 10;
// const wishlistLongDescriptionMaxLength = 10;
//
// // project
// const projectTitleMaxLength = 10;
// const projectGoalsMaxLength = 10;
// const projectLocationMaxLength = 10;
// const projectTechMaxLength = 10;
// const projectMemberNameMaxLength = 10;
// const projectEmailMaxLength = 10;
// const projectGitMaxLength = 10;
// const projectTeampageMaxLength = 10;
// const projectVideoMaxLength = 10;
// const projectDescriptionMaxLength = 10;
//
// // user
// const userFirstNameMaxLength = 10;
// const userMiddleInitialMaxLength = 10;
// const userLastNameMaxLength = 10;
// const userEmailMaxLength = 160;
// const userPasswordMaxLength = 10;
// const userRepasswordMaxLength = 10;
// const userResearchFieldsMaxLength = 10;
// const userAffiliationMaxLength = 10;
// const userTitleMaxLength = 10;
// const userMailingAddressMaxLength = 10;
// const userPhoneNumberMaxLength = 10;
// const userHighestDegreeMaxLength = 10;
//
// // post
// const postBodyMaxLength = 10;
//
// // bug
// const bugTitleMaxLength = 10;
// const bugDescriptionMaxLength = 10;
// const bugLongDescriptionMaxLength = 10;
//
// // suggestion
// const suggestionTitleMaxLength = 10;
// const suggestionDescriptionMaxLength = 10;
// const suggestionLongDescriptionMaxLength = 10;
